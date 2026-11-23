package com.fitnessapp.notification.service;

import com.fitnessapp.appAuth.model.entity.UserDeviceEntity;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserDeviceRepo;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.notification.dto.FcmDeliveryResult;
import com.fitnessapp.notification.dto.FcmNotificationRequest;
import com.fitnessapp.notification.dto.response.AppUserForNotificationResponseDto;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationServiceImpl implements FcmNotificationService {

    private static final int FCM_MULTICAST_LIMIT = 500;
    private final UserRepo userRepo;
    private final UserDeviceRepo userDeviceRepo;
    private final FirebaseApp firebaseApp;
    private final Validator validator;

    @Override
    @Transactional(readOnly = true)
    public List<AppUserForNotificationResponseDto> getAppUserForNotification() {
        List<UserEntity> list = userRepo.findAll();
        return list.stream().map(AppUserForNotificationResponseDto::from).toList();
    }

    @Override
    public FcmDeliveryResult sendNotificationToUserFromPortal(String email, FcmNotificationRequest request) {
        UserEntity user = userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App user not found"));
        return sendToUser(user.getId(), request);
    }

    @Override
    public FcmDeliveryResult sendNotificationToUsersFromPortal(Collection<String> email, FcmNotificationRequest request) {
        Collection<String> userId = new ArrayList<>();
        for (String email1 : email) {
            UserEntity user = userRepo.findByEmailIgnoreCase(email1).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App user not found"));
            userId.add(user.getId());
        }
        return sendToUsers(userId, request);
    }

    @Override
    public FcmDeliveryResult sendToUser(String userId, FcmNotificationRequest request) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId is required");
        }
        return sendToUsers(List.of(userId), request);
    }

    @Override
    public FcmDeliveryResult sendToUsers(Collection<String> userIds, FcmNotificationRequest request) {
        validateRequest(request);
        if (userIds == null || userIds.isEmpty()) return emptyResult();

        Set<String> distinctUserIds = new LinkedHashSet<>();
        for (String userId : userIds) {
            if (StringUtils.hasText(userId)) distinctUserIds.add(userId);
        }
        if (distinctUserIds.isEmpty()) return emptyResult();

        List<UserDeviceEntity> devices = userDeviceRepo
                .findAllByUser_IdInAndNotificationEnabledTrue(distinctUserIds);
        if (devices.isEmpty()) return emptyResult();

        int successCount = 0;
        int failureCount = 0;
        int removedInvalidTokenCount = 0;
        int attemptedDevices = 0;

        // FCM accepts at most 500 registration tokens in one multicast request.
        for (int start = 0; start < devices.size(); start += FCM_MULTICAST_LIMIT) {
            List<UserDeviceEntity> chunk = devices.subList(
                    start, Math.min(start + FCM_MULTICAST_LIMIT, devices.size()));
            List<String> tokens = chunk.stream().map(UserDeviceEntity::getFcmToken).toList();
            MulticastMessage message = buildMessage(tokens, request);
            attemptedDevices += chunk.size();

            final BatchResponse batchResponse;
            try {
                batchResponse = FirebaseMessaging.getInstance(firebaseApp).sendEachForMulticast(message);
            } catch (FirebaseMessagingException exception) {
                // Do not blindly retry: FCM may have accepted a request before a transport failure.
                log.error("FCM multicast request failed. errorCode={}, devices={}",
                        exception.getMessagingErrorCode(), chunk.size());
                throw new FcmNotificationDeliveryException("FCM notification delivery failed", exception);
            }

            successCount += batchResponse.getSuccessCount();
            failureCount += batchResponse.getFailureCount();
            removedInvalidTokenCount += pruneConfirmedInvalidTokens(chunk, batchResponse);
        }
        log.info("FCM delivery Success Counts: {}, Failed Count: {}", successCount, failureCount);
        return FcmDeliveryResult.builder()
                .targetedDevices(devices.size())
                .attemptedDevices(attemptedDevices)
                .successCount(successCount)
                .failureCount(failureCount)
                .removedInvalidTokenCount(removedInvalidTokenCount)
                .build();
    }

    private MulticastMessage buildMessage(List<String> tokens, FcmNotificationRequest request) {
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());
        if (StringUtils.hasText(request.getImageUrl())) {
            notificationBuilder.setImage(request.getImageUrl());
        }

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notificationBuilder.build());
        if (request.getData() != null && !request.getData().isEmpty()) {
            messageBuilder.putAllData(new LinkedHashMap<>(request.getData()));
        }
        return messageBuilder.build();
    }

    private int pruneConfirmedInvalidTokens(List<UserDeviceEntity> devices, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        int removed = 0;
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful() || sendResponse.getException() == null) continue;

            MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED) {
                UserDeviceEntity device = devices.get(i);
                // Compare-and-delete: a concurrent token refresh must not lose its new token.
                removed += (int) userDeviceRepo.deleteByIdAndFcmToken(device.getId(), device.getFcmToken());
            } else {
                // INVALID_ARGUMENT may indicate a malformed payload. Sender mismatch and
                // transient/quota failures are not proof that a registration token is invalid.
                log.warn("FCM delivery failed for device row id={},  with errorCode={}",
                        devices.get(i).getId(), errorCode);
            }
        }
        return removed;
    }

    private void validateRequest(FcmNotificationRequest request) {
        if (request == null) throw new IllegalArgumentException("Notification request is required");
        Set<ConstraintViolation<FcmNotificationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream().map(ConstraintViolation::getMessage)
                    .distinct().sorted().findFirst().orElse("Invalid notification request");
            throw new IllegalArgumentException(message);
        }
        if (request.getData() != null && request.getData().size() > 50) {
            throw new IllegalArgumentException("At most 50 data fields are supported");
        }
    }

    private FcmDeliveryResult emptyResult() {
        return FcmDeliveryResult.builder()
                .targetedDevices(0).attemptedDevices(0).successCount(0)
                .failureCount(0).removedInvalidTokenCount(0).build();
    }
}
