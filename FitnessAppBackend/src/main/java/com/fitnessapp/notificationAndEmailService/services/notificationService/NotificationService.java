package com.fitnessapp.notificationAndEmailService.services.notificationService;

import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendNotificationRequestDto;
import org.springframework.http.ResponseEntity;

public interface NotificationService {
    ResponseEntity<?> sendNotification(SendNotificationRequestDto sendNotificationRequestDto);

    ResponseEntity<?> sendBatchNotification(SendBatchNotificationRequestDto sendBatchNotificationRequestDto);

}