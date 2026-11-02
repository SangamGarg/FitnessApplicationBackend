package com.fitnessapp.notification.service;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.dto.response.MarkAllNotificationsReadResponseDto;
import com.fitnessapp.notification.dto.response.UnreadNotificationCountResponseDto;
import com.fitnessapp.notification.dto.response.UserNotificationResponseDto;
import com.fitnessapp.notification.model.entity.UserNotificationEntity;
import com.fitnessapp.notification.model.entity.UserNotificationOutboxEntity;
import com.fitnessapp.notification.model.enums.NotificationOutboxStatus;
import com.fitnessapp.notification.repo.UserNotificationOutboxRepository;
import com.fitnessapp.notification.repo.UserNotificationRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final UserRepo userRepo;
    private final UserNotificationRepository notificationRepository;
    private final UserNotificationOutboxRepository outboxRepository;
    private final Validator validator;

    @Override
    @Transactional
    public UUID createForUser(String userId, FcmNotificationRequest request) {
        validateRequest(request);
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient account not found"));
        Instant now = Instant.now();
        UserNotificationEntity notification = notificationRepository.save(
                UserNotificationEntity.builder()
                        .user(user)
                        .title(request.getTitle())
                        .body(request.getBody())
                        .imageUrl(request.getImageUrl())
                        .data(request.getData() == null ? Map.of() : Map.copyOf(request.getData()))
                        .build());
        outboxRepository.save(UserNotificationOutboxEntity.builder()
                .notification(notification)
                .status(NotificationOutboxStatus.PENDING)
                .attemptCount(0)
                .nextAttemptAt(now)
                .build());
        return notification.getId();
    }

    @Override
    @Transactional
    public int createForUsers(Collection<String> userIds, FcmNotificationRequest request) {
        validateRequest(request);
        if (userIds == null || userIds.isEmpty()) return 0;
        Set<String> recipients = new LinkedHashSet<>();
        userIds.stream().filter(StringUtils::hasText).forEach(recipients::add);
        int created = 0;
        for (String userId : recipients) {
            UserEntity user = userRepo.findById(userId).orElse(null);
            if (user == null) continue;
            UserNotificationEntity notification = notificationRepository.save(
                    UserNotificationEntity.builder()
                            .user(user)
                            .title(request.getTitle())
                            .body(request.getBody())
                            .imageUrl(request.getImageUrl())
                            .data(request.getData() == null ? Map.of() : Map.copyOf(request.getData()))
                            .build());
            outboxRepository.save(UserNotificationOutboxEntity.builder()
                    .notification(notification)
                    .status(NotificationOutboxStatus.PENDING)
                    .attemptCount(0)
                    .nextAttemptAt(Instant.now())
                    .build());
            created++;
        }
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserNotificationResponseDto> getMyNotifications(boolean unreadOnly, Pageable pageable) {
        String userId = currentUserId();
        Page<UserNotificationEntity> page = unreadOnly
                ? notificationRepository.findAllByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findAllByUser_IdOrderByCreatedAtDesc(userId, pageable);
        return page.map(UserNotificationResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponseDto getMyUnreadCount() {
        return new UnreadNotificationCountResponseDto(
                notificationRepository.countByUser_IdAndReadAtIsNull(currentUserId()));
    }

    @Override
    @Transactional
    public void markMyNotificationAsRead(UUID notificationId) {
        String userId = currentUserId();
        if (!notificationRepository.existsByIdAndUser_Id(notificationId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found");
        }
        notificationRepository.markAsRead(notificationId, userId, Instant.now());
    }

    @Override
    @Transactional
    public MarkAllNotificationsReadResponseDto markAllMyNotificationsAsRead() {
        return new MarkAllNotificationsReadResponseDto(
                notificationRepository.markAllAsRead(currentUserId(), Instant.now()));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !StringUtils.hasText(authentication.getName())
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private void validateRequest(FcmNotificationRequest request) {
        if (request == null) throw new IllegalArgumentException("Notification request is required");
        Set<ConstraintViolation<FcmNotificationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream().map(ConstraintViolation::getMessage)
                    .distinct().sorted().findFirst().orElse("Invalid notification request");
            throw new IllegalArgumentException(message);
        }
        if (request.getData() != null && request.getData().size() > 49) {
            // Stage 4 reserves one FCM data key for client-side notification de-duplication.
            throw new IllegalArgumentException("At most 49 custom data fields are supported");
        }
    }
}
