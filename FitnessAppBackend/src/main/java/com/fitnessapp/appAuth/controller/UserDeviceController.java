package com.fitnessapp.appAuth.controller;

import com.fitnessapp.appAuth.model.dtos.requestDto.DeviceNotificationPreferenceRequestDto;
import com.fitnessapp.appAuth.model.dtos.requestDto.DeviceRegistrationRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserDeviceResponseDto;
import com.fitnessapp.appAuth.service.UserDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtil.APP_AUTH_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(APP_AUTH_ENDPOINT + "/devices")
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    /** Register on login and whenever FirebaseMessagingService.onNewToken() fires. */
    @PostMapping("/register")
    public ResponseEntity<UserDeviceResponseDto> register(@RequestBody @Valid DeviceRegistrationRequestDto request) {
        log.info("Received request to register/update device: deviceId={}", request.getDeviceId());
        UserDeviceResponseDto response = userDeviceService.registerOrUpdateDevice(request);
        log.info("Device registered/updated successfully: deviceId={}", request.getDeviceId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserDeviceResponseDto>> getMyDevices() {
        log.info("Received request to fetch user's registered devices");
        List<UserDeviceResponseDto> devices = userDeviceService.getMyDevices();
        log.info("Retrieved {} device(s) for user", devices.size());
        return ResponseEntity.ok(devices);
    }

    @PatchMapping("/{deviceId}/notifications")
    public ResponseEntity<UserDeviceResponseDto> updateNotifications(
            @PathVariable String deviceId,
            @RequestBody @Valid DeviceNotificationPreferenceRequestDto request) {
        log.info("Received request to update notification preferences for device: deviceId={}, enabled={}",
                deviceId, request.getEnabled());
        UserDeviceResponseDto response = userDeviceService.updateNotificationPreference(deviceId, request.getEnabled());
        log.info("Notification preference updated successfully for device: deviceId={}", deviceId);
        return ResponseEntity.ok(response);
    }

    /** Call on logout after revoking this installation's refresh-token family. */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> unregister(@PathVariable String deviceId) {
        log.info("Received request to unregister device: deviceId={}", deviceId);
        userDeviceService.unregisterDevice(deviceId);
        log.info("Device unregistered successfully: deviceId={}", deviceId);
        return ResponseEntity.noContent().build();
    }
}