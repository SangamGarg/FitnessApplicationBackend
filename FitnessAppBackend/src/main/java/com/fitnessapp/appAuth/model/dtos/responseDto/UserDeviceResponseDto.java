package com.fitnessapp.appAuth.model.dtos.responseDto;

import com.fitnessapp.appAuth.model.enums.DevicePlatform;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/** Deliberately excludes the FCM registration token. */
@Value
@Builder
public class UserDeviceResponseDto {
    Long id;
    String deviceId;
    DevicePlatform platform;
    String deviceName;
    String appVersion;
    Boolean notificationEnabled;
    Instant lastActiveAt;
    Instant tokenUpdatedAt;
    Instant createdAt;
    Instant updatedAt;
}
