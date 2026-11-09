package com.fitnessapp.notification.service;

import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.dto.response.MarkAllNotificationsReadResponseDto;
import com.fitnessapp.notification.dto.response.UnreadNotificationCountResponseDto;
import com.fitnessapp.notification.dto.response.UserNotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.UUID;

public interface UserNotificationService {

    /** Create the in-app notification and its FCM outbox record in one database transaction. */
    UUID createForUser(String userId, FcmNotificationRequest request);

    /** Create one in-app notification/outbox record per distinct recipient. */
    int createForUsers(Collection<String> userIds,  FcmNotificationRequest request);

    Page<UserNotificationResponseDto> getMyNotifications(boolean unreadOnly, Pageable pageable);

    UnreadNotificationCountResponseDto getMyUnreadCount();

    void markMyNotificationAsRead(UUID notificationId);

    MarkAllNotificationsReadResponseDto markAllMyNotificationsAsRead();
}
