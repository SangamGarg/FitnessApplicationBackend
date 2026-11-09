package com.fitnessapp.notification.model.enums;

public enum NotificationOutboxStatus {
    PENDING,
    PROCESSING,
    RETRY_WAIT,
    SENT,
    NO_DEVICES,
    FAILED
}
