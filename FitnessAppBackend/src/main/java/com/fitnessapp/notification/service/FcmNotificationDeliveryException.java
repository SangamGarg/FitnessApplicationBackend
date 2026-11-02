package com.fitnessapp.notification.service;

public class FcmNotificationDeliveryException extends RuntimeException {
    public FcmNotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
