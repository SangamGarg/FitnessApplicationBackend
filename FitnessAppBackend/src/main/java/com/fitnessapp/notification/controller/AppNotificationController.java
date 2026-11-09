package com.fitnessapp.notification.controller;

import com.fitnessapp.notification.dto.response.MarkAllNotificationsReadResponseDto;
import com.fitnessapp.notification.dto.response.UnreadNotificationCountResponseDto;
import com.fitnessapp.notification.dto.response.UserNotificationResponseDto;
import com.fitnessapp.notification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_NOTIFICATIONS_ENDPOINT;

@RestController
@RequestMapping(APP_NOTIFICATIONS_ENDPOINT)
@RequiredArgsConstructor
public class AppNotificationController {

    private static final int MAX_PAGE_SIZE = 100;
    private final UserNotificationService userNotificationService;

    @GetMapping
    public ResponseEntity<Page<UserNotificationResponseDto>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        if (page < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (size < 1) throw new IllegalArgumentException("size must be at least 1");
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userNotificationService.getMyNotifications(unreadOnly, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponseDto> getUnreadCount() {
        return ResponseEntity.ok(userNotificationService.getMyUnreadCount());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        userNotificationService.markMyNotificationAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<MarkAllNotificationsReadResponseDto> markAllAsRead() {
        return ResponseEntity.ok(userNotificationService.markAllMyNotificationsAsRead());
    }
}
