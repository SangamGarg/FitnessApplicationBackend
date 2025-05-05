package com.fitnessapp.notificationAndEmailService.services.notificationService;

import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendNotificationRequestDto;
import com.google.api.services.storage.Storage;
import com.google.firebase.messaging.Message;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface NotificationService {
    ResponseEntity<?> sendNotificationToDevice(SendNotificationRequestDto sendNotificationRequestDto);

    ResponseEntity<?> sendBatchNotification(SendBatchNotificationRequestDto sendBatchNotificationRequestDto);

}