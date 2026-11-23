package com.fitnessapp.notification.controller;

import com.fitnessapp.notification.dto.FcmDeliveryResult;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.dto.response.AppUserForNotificationResponseDto;
import com.fitnessapp.notification.service.FcmNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_NOTIFICATION_ENDPOINT;

//************************* RESTRICTED *************************//

@RestController
@RequestMapping(PORTAL_DEV_NOTIFICATION_ENDPOINT)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PortalNotificationController {

    private final FcmNotificationService fcmNotificationService;


    @GetMapping("/users")
    public ResponseEntity<List<AppUserForNotificationResponseDto>> getAppUserForNotification() {
        log.info("Fetching app user for notification portal");
        return ResponseEntity.ok(fcmNotificationService.getAppUserForNotification());
    }


    /**
     * Send a notification to a specific user.
     */
    @PostMapping("/users/{userId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FcmDeliveryResult> sendToUser(
            @PathVariable @NotBlank String userId,
            @Valid @RequestBody FcmNotificationRequest request
    ) {
        FcmDeliveryResult result =
                fcmNotificationService.sendToUser(userId, request);

        return ResponseEntity.ok(result);
    }

    /**
     * Send notification to multiple users.
     */
    @PostMapping("/users/bulk")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FcmDeliveryResult> sendToUsers(
            @Valid @RequestBody BulkFcmNotificationRequest request
    ) {
        FcmDeliveryResult result =
                fcmNotificationService.sendToUsers(
                        request.userIds(),
                        request.notification()
                );

        return ResponseEntity.ok(result);
    }

    public record BulkFcmNotificationRequest(
            @NotEmpty
            Collection<@NotBlank String> userIds,

            @Valid
            FcmNotificationRequest notification
    ) {
    }
}