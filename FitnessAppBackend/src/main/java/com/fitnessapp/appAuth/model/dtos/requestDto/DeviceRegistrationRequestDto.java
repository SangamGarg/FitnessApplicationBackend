package com.fitnessapp.appAuth.model.dtos.requestDto;

import com.fitnessapp.appAuth.model.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeviceRegistrationRequestDto {

    /** Random UUID generated once and persisted by this app installation. */
    @NotBlank
    @Size(max = 64)
    private String deviceId;

    @NotBlank
    @Size(max = 4096)
    private String fcmToken;

    @NotNull
    private DevicePlatform platform;

    @Size(max = 120)
    private String deviceName;

    @Size(max = 40)
    private String appVersion;

    /** Null means keep the existing preference; a new device defaults to enabled. */
    private Boolean notificationEnabled;
}
