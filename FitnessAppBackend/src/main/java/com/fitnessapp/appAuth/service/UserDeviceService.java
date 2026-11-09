package com.fitnessapp.appAuth.service;

import com.fitnessapp.appAuth.model.dtos.requestDto.DeviceRegistrationRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserDeviceResponseDto;

import java.util.List;

public interface UserDeviceService {
    UserDeviceResponseDto registerOrUpdateDevice(DeviceRegistrationRequestDto request);
    List<UserDeviceResponseDto> getMyDevices();
    UserDeviceResponseDto updateNotificationPreference(String deviceId, boolean enabled);
    void unregisterDevice(String deviceId);
}
