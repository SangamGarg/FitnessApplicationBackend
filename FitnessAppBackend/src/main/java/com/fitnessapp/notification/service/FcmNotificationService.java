package com.fitnessapp.notification.service;

import com.fitnessapp.notification.dto.FcmDeliveryResult;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.dto.response.AppUserForNotificationResponseDto;

import java.util.Collection;
import java.util.List;

/** Internal application service. Call this from trusted business services, not from a public client endpoint. */
public interface FcmNotificationService {
    List<AppUserForNotificationResponseDto> getAppUserForNotification();
    FcmDeliveryResult sendToUser(String userId, FcmNotificationRequest request);
    FcmDeliveryResult sendToUsers(Collection<String> userIds, FcmNotificationRequest request);
}
