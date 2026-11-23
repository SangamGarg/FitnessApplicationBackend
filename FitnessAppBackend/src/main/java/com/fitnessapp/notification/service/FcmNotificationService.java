package com.fitnessapp.notification.service;

import com.fitnessapp.notification.dto.FcmDeliveryResult;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.dto.response.AppUserForNotificationResponseDto;
import com.fitnessapp.notification.dto.response.PortalNotificationAnalyticsDto;
import com.fitnessapp.notification.dto.response.PortalNotificationHistoryDto;
import com.fitnessapp.notification.model.enums.NotificationOutboxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

/** Internal application service. Call this from trusted business services, not from a public client endpoint. */
public interface FcmNotificationService {
    List<AppUserForNotificationResponseDto> getAppUserForNotification();

    Page<PortalNotificationHistoryDto> getNotificationHistory(NotificationOutboxStatus status, Pageable pageable);
    PortalNotificationAnalyticsDto getDeliveryAnalytics();

    FcmDeliveryResult sendNotificationToUserFromPortal(String email, FcmNotificationRequest request);
    FcmDeliveryResult sendNotificationToUsersFromPortal(Collection<String> email, FcmNotificationRequest request);

    FcmDeliveryResult sendToUser(String userId, FcmNotificationRequest request);

    FcmDeliveryResult sendToUsers(Collection<String> userIds, FcmNotificationRequest request);
}
