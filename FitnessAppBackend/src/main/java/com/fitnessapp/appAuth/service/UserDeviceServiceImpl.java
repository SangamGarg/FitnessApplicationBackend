package com.fitnessapp.appAuth.service;

import com.fitnessapp.appAuth.model.dtos.requestDto.DeviceRegistrationRequestDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserDetailsResponseDto;
import com.fitnessapp.appAuth.model.dtos.responseDto.UserDeviceResponseDto;
import com.fitnessapp.appAuth.model.entity.UserDeviceEntity;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserDeviceRepo;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.utils.appUtil.AppResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserDeviceRepo userDeviceRepo;
    private final UserRepo userRepo;

    /**
     * Upserts one installation. A deviceId is globally unique, so signing in to another
     * account transfers this installation without disturbing the account's other devices.
     * An FCM token already attached to a different installation is treated as stale and
     * detached before the new registration is saved.
     */
    @Override
    @Transactional
    public UserDeviceResponseDto registerOrUpdateDevice(DeviceRegistrationRequestDto request) {
        String authenticatedUserId = currentUserId();
        log.info("Processing device registration: userId={}, deviceId={}, platform={}",
                authenticatedUserId, request.getDeviceId(), request.getPlatform());

        UserEntity currentUser = userRepo.findByIdForUpdate(authenticatedUserId)
                .orElseThrow(() -> {
                    log.warn("Device registration failed: Authenticated account not found for userId={}", authenticatedUserId);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated account was not found");
                });

        Instant now = Instant.now();
        UserDeviceEntity device = userDeviceRepo.findByDeviceIdForUpdate(request.getDeviceId()).orElse(null);

        // FCM may issue a new token after reinstall/restore. Keep only the newest owner of a token.
        UserDeviceEntity tokenOwner = userDeviceRepo.findByFcmTokenForUpdate(request.getFcmToken()).orElse(null);
        if (tokenOwner != null && (device == null || !tokenOwner.getId().equals(device.getId()))) {
            log.info("Detaching stale token registration from previous installation: previousDeviceId={}, newDeviceId={}",
                    tokenOwner.getDeviceId(), request.getDeviceId());
            userDeviceRepo.delete(tokenOwner);
            userDeviceRepo.flush();
        }

        if (device == null) {
            log.info("Creating new device installation record: userId={}, deviceId={}",
                    authenticatedUserId, request.getDeviceId());
            device = UserDeviceEntity.builder()
                    .deviceId(request.getDeviceId())
                    .fcmToken(request.getFcmToken())
                    .platform(request.getPlatform())
                    .deviceName(request.getDeviceName())
                    .appVersion(request.getAppVersion())
                    .notificationEnabled(request.getNotificationEnabled() == null || request.getNotificationEnabled())
                    .lastActiveAt(now)
                    .tokenUpdatedAt(now)
                    .user(currentUser)
                    .build();
        } else {
            boolean tokenChanged = !request.getFcmToken().equals(device.getFcmToken());
            boolean accountChanged = device.getUser() == null
                    || !authenticatedUserId.equals(device.getUser().getId());

            log.info("Updating existing device installation: deviceId={}, tokenChanged={}, accountChanged={}",
                    request.getDeviceId(), tokenChanged, accountChanged);

            // Reassign this installation when another account signs in.
            device.setUser(currentUser);
            device.setFcmToken(request.getFcmToken());
            device.setPlatform(request.getPlatform());
            device.setDeviceName(request.getDeviceName());
            device.setAppVersion(request.getAppVersion());
            if (request.getNotificationEnabled() != null) {
                device.setNotificationEnabled(request.getNotificationEnabled());
            } else if (device.getNotificationEnabled() == null) {
                device.setNotificationEnabled(true);
            }
            device.setLastActiveAt(now);
            if (tokenChanged) {
                device.setTokenUpdatedAt(now);
            }
            if (accountChanged) {
                log.info("App installation reassigned to authenticated account: deviceId={}, newUserId={}",
                        request.getDeviceId(), authenticatedUserId);
            }
        }

        UserDeviceEntity saved = userDeviceRepo.saveAndFlush(device);
        log.info("Device successfully registered/updated: id={}, deviceId={}, userId={}",
                saved.getId(), saved.getDeviceId(), authenticatedUserId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDeviceResponseDto> getMyDevices() {
        String userId = currentUserId();
        log.debug("Fetching registered devices for userId={}", userId);
        List<UserDeviceEntity> devices = userDeviceRepo.findAllByUser_IdOrderByLastActiveAtDesc(userId);
        log.debug("Retrieved {} device(s) for userId={}", devices.size(), userId);
        return devices.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public UserDeviceResponseDto updateNotificationPreference(String deviceId, boolean enabled) {
        String userId = currentUserId();
        log.info("Updating notification preference: userId={}, deviceId={}, enabled={}", userId, deviceId, enabled);
        UserDeviceEntity device = findOwnedDevice(deviceId);
        device.setNotificationEnabled(enabled);
        device.setLastActiveAt(Instant.now());
        UserDeviceEntity updated = userDeviceRepo.save(device);
        log.info("Notification preference updated: deviceId={}, enabled={}", deviceId, enabled);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void unregisterDevice(String deviceId) {
        String userId = currentUserId();
        log.info("Unregistering device: userId={}, deviceId={}", userId, deviceId);
        // Ownership is part of the delete predicate; another user's device cannot be removed.
        long deletedCount = userDeviceRepo.deleteByDeviceIdAndUser_Id(deviceId, userId);
        if (deletedCount > 0) {
            log.info("Device successfully unregistered: userId={}, deviceId={}", userId, deviceId);
        } else {
            log.warn("Unregister device skipped: No matching device found for userId={}, deviceId={}", userId, deviceId);
        }
    }

    private UserDeviceEntity findOwnedDevice(String deviceId) {
        String userId = currentUserId();
        return userDeviceRepo.findByDeviceIdAndUser_Id(deviceId, userId)
                .orElseThrow(() -> {
                    log.warn("Device access rejected: Device not found or not owned by userId={}: deviceId={}", userId, deviceId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found");
                });
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private UserDeviceResponseDto toResponse(UserDeviceEntity device) {
        return UserDeviceResponseDto.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .platform(device.getPlatform())
                .deviceName(device.getDeviceName())
                .appVersion(device.getAppVersion())
                .notificationEnabled(device.getNotificationEnabled())
                .lastActiveAt(device.getLastActiveAt())
                .tokenUpdatedAt(device.getTokenUpdatedAt())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }

    private ResponseEntity<UserDeviceResponseDto> errorResponseDevice(HttpStatus status, String message) {
        log.warn(message);
        return ResponseEntity.status(status).body(UserDeviceResponseDto.builder().status(AppResponseStatus.FAILED.name()).statusCode(status.value()).message(message).build());
    }
}