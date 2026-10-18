package com.fitnessapp.notification.notificationService;

import com.fitnessapp.notification.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notification.notificationDtos.request.SendNotificationRequestDto;
import org.springframework.http.ResponseEntity;

public interface NotificationService {
    ResponseEntity<?> sendNotification(SendNotificationRequestDto sendNotificationRequestDto);

    ResponseEntity<?> sendBatchNotification(SendBatchNotificationRequestDto sendBatchNotificationRequestDto);

}