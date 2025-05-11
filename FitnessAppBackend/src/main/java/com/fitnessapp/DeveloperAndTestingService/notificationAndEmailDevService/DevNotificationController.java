package com.fitnessapp.DeveloperAndTestingService.notificationAndEmailDevService;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.services.notificationService.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_NOTIFICATION)
@Validated
public class DevNotificationController {
    private final NotificationService notificationService;

    public DevNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint for sending notification to device/devices
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody @Valid SendNotificationRequestDto sendNotificationRequestDto) {
        // Handle single device notification
        return notificationService.sendNotification(sendNotificationRequestDto);
    }

    /**
     * Endpoint for sending batch notifications to multiple devices
     */
    @PostMapping("/send-batch")
    public ResponseEntity<?> sendBatchNotification(@RequestBody SendBatchNotificationRequestDto sendBatchNotificationRequestDto) {
        // Handle batch notification
        return notificationService.sendBatchNotification(sendBatchNotificationRequestDto);
    }
}
