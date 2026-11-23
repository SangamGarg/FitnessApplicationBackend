package com.fitnessapp.notification.dto.response;

import com.fitnessapp.notification.model.entity.UserNotificationOutboxEntity;
import com.fitnessapp.notification.model.enums.NotificationOutboxStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PortalNotificationHistoryDto {
    UUID notificationId;
    String recipientEmail;
    String title;
    String body;
    NotificationOutboxStatus deliveryStatus;
    int attemptCount;
    String lastError;
    Instant createdAt;
    Instant updatedAt;
    Instant readAt;

    public static PortalNotificationHistoryDto from(UserNotificationOutboxEntity outbox) {
        var notification = outbox.getNotification();
        return PortalNotificationHistoryDto.builder()
                .notificationId(notification.getId())
                .recipientEmail(notification.getUser().getEmail())
                .title(notification.getTitle())
                .body(notification.getBody())
                .deliveryStatus(outbox.getStatus())
                .attemptCount(outbox.getAttemptCount())
                .lastError(outbox.getLastError())
                .createdAt(notification.getCreatedAt())
                .updatedAt(outbox.getUpdatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
