package com.fitnessapp.notification.dto.response;

import com.fitnessapp.notification.model.enums.NotificationOutboxStatus;
import lombok.Value;

import java.util.Map;

@Value
public class PortalNotificationAnalyticsDto {
    long total;
    long pending;
    long processing;
    long retryWait;
    long sent;
    long noDevices;
    long failed;
    double deliveryRatePercent;

    public static PortalNotificationAnalyticsDto from(Map<NotificationOutboxStatus, Long> counts) {
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long sent = counts.getOrDefault(NotificationOutboxStatus.SENT, 0L);
        long noDevices = counts.getOrDefault(NotificationOutboxStatus.NO_DEVICES, 0L);
        long failed = counts.getOrDefault(NotificationOutboxStatus.FAILED, 0L);
        long completed = sent + noDevices + failed;
        return new PortalNotificationAnalyticsDto(total,
                counts.getOrDefault(NotificationOutboxStatus.PENDING, 0L),
                counts.getOrDefault(NotificationOutboxStatus.PROCESSING, 0L),
                counts.getOrDefault(NotificationOutboxStatus.RETRY_WAIT, 0L),
                sent, noDevices, failed,
                completed == 0 ? 0.0 : Math.round((sent * 10000.0) / completed) / 100.0);
    }
}
