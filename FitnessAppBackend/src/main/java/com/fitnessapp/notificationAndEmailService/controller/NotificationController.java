package com.fitnessapp.notificationAndEmailService.controller;

import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities.AppConstantsNotificationAndEmailService;
import com.fitnessapp.notificationAndEmailService.services.notificationService.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstantsNotificationAndEmailService.API_PREFIX_NOTIFICATION)
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint for sending notification to device/devices
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendNotificationToOneDevice(@RequestBody @Valid SendNotificationRequestDto sendNotificationRequestDto) {
        // Handle single device notification
        return notificationService.sendNotificationToDevice(sendNotificationRequestDto);
    }

    /**
     * Endpoint for sending batch notifications to multiple devices
     */
    @PostMapping("/send/batch")
    public ResponseEntity<?> sendBatchNotification(@RequestBody SendBatchNotificationRequestDto sendBatchNotificationRequestDto) {
        // Handle batch notification
        return notificationService.sendBatchNotification(sendBatchNotificationRequestDto);
    }
}