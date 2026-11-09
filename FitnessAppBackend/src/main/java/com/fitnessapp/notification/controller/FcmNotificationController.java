package com.fitnessapp.notification.controller;

import com.fitnessapp.notification.dto.FcmDeliveryResult;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.service.FcmNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

//************************* RESTRICTED *************************//

@RestController
@RequestMapping("/api/v1/portal/notifications")
@RequiredArgsConstructor
@Validated
public class FcmNotificationController {

    private final FcmNotificationService fcmNotificationService;

    /**
     * Send notification to a specific user.
     */
    @PostMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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