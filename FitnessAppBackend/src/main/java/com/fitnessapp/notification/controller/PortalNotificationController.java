package com.fitnessapp.notification.controller;

import com.fitnessapp.notification.dto.FcmDeliveryResult;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.dto.response.AppUserForNotificationResponseDto;
import com.fitnessapp.notification.dto.response.PortalNotificationAnalyticsDto;
import com.fitnessapp.notification.dto.response.PortalNotificationHistoryDto;
import com.fitnessapp.notification.model.enums.NotificationOutboxStatus;
import com.fitnessapp.notification.service.FcmNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping("/history")
    public ResponseEntity<Page<PortalNotificationHistoryDto>> getNotificationHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) NotificationOutboxStatus status) {
        if (page < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (size < 1) throw new IllegalArgumentException("size must be at least 1");
        return ResponseEntity.ok(fcmNotificationService.getNotificationHistory(status,
                PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/history/analytics")
    public ResponseEntity<PortalNotificationAnalyticsDto> getDeliveryAnalytics() {
        return ResponseEntity.ok(fcmNotificationService.getDeliveryAnalytics());
    }


    @GetMapping("/users")
    public ResponseEntity<List<AppUserForNotificationResponseDto>> getAppUserForNotification() {
        log.info("Fetching app user for notification portal");
        return ResponseEntity.ok(fcmNotificationService.getAppUserForNotification());
    }


    /**
     * Send a notification to a specific user.
     */
    @PostMapping("/user/send")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FcmDeliveryResult> sendToUser(
            @RequestParam() String userEmail,
            @Valid @RequestBody FcmNotificationRequest request
    ) {
        FcmDeliveryResult result =
                fcmNotificationService.sendNotificationToUserFromPortal(userEmail, request);

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
                fcmNotificationService.sendNotificationToUsersFromPortal(
                        request.email(),
                        request.notification()
                );

        return ResponseEntity.ok(result);
    }

    public record BulkFcmNotificationRequest(
            @NotEmpty
            Collection<@NotBlank String> email,

            @Valid
            FcmNotificationRequest notification
    ) {
    }
}
