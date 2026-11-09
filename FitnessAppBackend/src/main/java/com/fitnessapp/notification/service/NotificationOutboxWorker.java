package com.fitnessapp.notification.service;

import com.fitnessapp.notification.dto.FcmDeliveryResult;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.model.entity.UserNotificationEntity;
import com.fitnessapp.notification.model.entity.UserNotificationOutboxEntity;
import com.fitnessapp.notification.model.enums.NotificationOutboxStatus;
import com.fitnessapp.notification.repo.UserNotificationOutboxRepository;
import com.fitnessapp.notification.repo.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxWorker {

    private static final int MAX_ATTEMPTS = 8;
    private static final int MAX_BATCH_SIZE = 100;
    private static final Duration STALE_LOCK_AGE = Duration.ofMinutes(10);
    private static final Duration MAX_RETRY_DELAY = Duration.ofHours(6);

    private final UserNotificationOutboxRepository outboxRepository;
    private final UserNotificationRepository notificationRepository;
    private final FcmNotificationService fcmNotificationService;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.notifications.outbox.batch-size:50}")
    private int configuredBatchSize;

    @Scheduled(fixedDelayString = "${app.notifications.outbox.fixed-delay-ms:5000}")
    public void processDueNotifications() {
        int batchSize = Math.max(1, Math.min(configuredBatchSize, MAX_BATCH_SIZE));
        List<DeliveryTask> tasks = claimDueTasks(batchSize);
        for (DeliveryTask task : tasks) {
            process(task);
        }
    }

    private List<DeliveryTask> claimDueTasks(int batchSize) {
        List<DeliveryTask> tasks = transactionTemplate.execute(status -> {
            Instant now = Instant.now();
            List<UserNotificationOutboxEntity> rows = outboxRepository.lockDueBatch(
                    now, now.minus(STALE_LOCK_AGE), batchSize);
            List<DeliveryTask> claimed = new ArrayList<>(rows.size());
            for (UserNotificationOutboxEntity row : rows) {
                UserNotificationEntity notification = notificationRepository.findById(row.getNotification().getId())
                        .orElse(null);
                if (notification == null) {
                    row.setStatus(NotificationOutboxStatus.FAILED);
                    row.setLastError("Notification record is missing");
                    row.setLockedAt(null);
                    continue;
                }
                row.setStatus(NotificationOutboxStatus.PROCESSING);
                row.setAttemptCount(row.getAttemptCount() + 1);
                row.setLockedAt(now);
                claimed.add(new DeliveryTask(
                        row.getId(), notification.getId(), notification.getUser().getId(),
                        notification.getTitle(), notification.getBody(), notification.getImageUrl(),
                        notification.getData() == null ? Map.of() : Map.copyOf(notification.getData()),
                        row.getAttemptCount()));
            }
            outboxRepository.saveAll(rows);
            return claimed;
        });
        return tasks == null ? List.of() : tasks;
    }

    private void process(DeliveryTask task) {
        Map<String, String> data = new LinkedHashMap<>(task.data());
        // Reserved key enables mobile clients to ignore duplicate push deliveries on retries.
        data.put("notificationId", task.notificationId().toString());
        FcmNotificationRequest request = FcmNotificationRequest.builder()
                .title(task.title())
                .body(task.body())
                .imageUrl(task.imageUrl())
                .data(data)
                .build();

        try {
            FcmDeliveryResult result = fcmNotificationService.sendToUser(task.userId(), request);
            if (result.getTargetedDevices() == 0) {
                complete(task.outboxId(), NotificationOutboxStatus.NO_DEVICES,
                        "No enabled device registrations at delivery time");
            } else if (result.getFailureCount() == 0) {
                complete(task.outboxId(), NotificationOutboxStatus.SENT, null);
            } else {
                retryOrFail(task, "FCM reported " + result.getFailureCount() + " failed device delivery(s)");
            }
        } catch (Exception exception) {
            // Do not log exception messages: SDK messages can contain request details.
            log.warn("Notification push attempt failed; notificationId={}, attempt={}",
                    task.notificationId(), task.attempt());
            retryOrFail(task, "FCM delivery attempt failed");
        }
    }

    private void retryOrFail(DeliveryTask task, String safeError) {
        transactionTemplate.executeWithoutResult(status -> {
            UserNotificationOutboxEntity row = outboxRepository.findById(task.outboxId()).orElse(null);
            if (row == null || row.getStatus() != NotificationOutboxStatus.PROCESSING) return;
            row.setLockedAt(null);
            row.setLastError(safeError);
            if (row.getAttemptCount() >= MAX_ATTEMPTS) {
                row.setStatus(NotificationOutboxStatus.FAILED);
            } else {
                row.setStatus(NotificationOutboxStatus.RETRY_WAIT);
                row.setNextAttemptAt(Instant.now().plus(retryDelay(row.getAttemptCount())));
            }
            outboxRepository.save(row);
        });
    }

    private void complete(UUID outboxId, NotificationOutboxStatus finalStatus, String safeError) {
        transactionTemplate.executeWithoutResult(status -> {
            UserNotificationOutboxEntity row = outboxRepository.findById(outboxId).orElse(null);
            if (row == null || row.getStatus() != NotificationOutboxStatus.PROCESSING) return;
            row.setStatus(finalStatus);
            row.setLockedAt(null);
            row.setLastError(safeError);
            outboxRepository.save(row);
        });
    }

    private Duration retryDelay(int attempt) {
        long seconds = Math.min(30L * (1L << Math.min(Math.max(attempt - 1, 0), 10)),
                MAX_RETRY_DELAY.toSeconds());
        return Duration.ofSeconds(seconds);
    }

    private record DeliveryTask(
            UUID outboxId,
            UUID notificationId,
            String userId,
            String title,
            String body,
            String imageUrl,
            Map<String, String> data,
            int attempt) {
    }
}
