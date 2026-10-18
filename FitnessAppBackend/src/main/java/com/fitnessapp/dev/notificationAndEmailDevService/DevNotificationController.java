package com.fitnessapp.dev.notificationAndEmailDevService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.dev.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.notification.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notification.notificationDtos.request.SendNotificationRequestDto;
import com.fitnessapp.notification.notificationService.NotificationService;
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
@Tag(name = "Dev Notification", description = "Endpoints for dev notification")
public class DevNotificationController {
    private final NotificationService notificationService;

    public DevNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Endpoint for sending notification to device/devices
     */
    @Operation(summary = "send Notification", description = """
        Executes the send notification operation.
        """)
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody @Valid SendNotificationRequestDto sendNotificationRequestDto) {
        // Handle single device notification
        return notificationService.sendNotification(sendNotificationRequestDto);
    }

    /**
     * Endpoint for sending batch notifications to multiple devices
     */
    @Operation(summary = "send Batch Notification", description = """
        Executes the send batch notification operation.
        """)
    @PostMapping("/send-batch")
    public ResponseEntity<?> sendBatchNotification(@RequestBody SendBatchNotificationRequestDto sendBatchNotificationRequestDto) {
        // Handle batch notification
        return notificationService.sendBatchNotification(sendBatchNotificationRequestDto);
    }
}
