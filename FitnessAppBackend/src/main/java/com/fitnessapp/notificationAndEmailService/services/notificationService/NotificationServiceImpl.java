package com.fitnessapp.notificationAndEmailService.services.notificationService;

import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendBatchNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendNotificationRequestDto;
import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.response.SendNotificationResponseDto;
import com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities.AppConstantsNotificationAndEmailService;
import com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities.FcmUtility;
import com.google.firebase.messaging.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Override
    public ResponseEntity<?> sendNotificationToDevice(SendNotificationRequestDto requestDto) {
        if (hasMoreThanOneTarget(requestDto)) {
            return ResponseEntity.badRequest().body("Only one of deviceTokens, deviceGroupKey, topic, or condition should be provided.");
        }
        if (requestDto.getDeviceTokens() != null && !requestDto.getDeviceTokens().isEmpty()) {
            // Send notification to one or multiple devices
            return sendNotificationToDevices(requestDto.getDeviceTokens(), requestDto);
        } else if (requestDto.getDeviceGroupKey() != null && !requestDto.getDeviceGroupKey().trim().isEmpty()) {
            // Send notification to a device group
            return sendNotificationToDeviceGroup(requestDto.getDeviceGroupKey(), requestDto);
        } else if (requestDto.getTopic() != null && !requestDto.getTopic().trim().isEmpty()) {
            // Send notification to a topic
            return sendNotificationToTopic(requestDto.getTopic(), requestDto);
        } else if (requestDto.getCondition() != null && !requestDto.getCondition().trim().isEmpty()) {
            // Send notification using condition
            return sendNotificationWithCondition(requestDto.getCondition(), requestDto);
        } else {
            return ResponseEntity.badRequest().body("Invalid request, at least one target type (deviceTokens, deviceGroupKey, topic, condition) should be provided.");
        }
    }

    private ResponseEntity<?> sendNotificationWithCondition(String condition, SendNotificationRequestDto requestDto) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(requestDto.getTitle())
                            .setBody(requestDto.getBody())
                            .setImage(requestDto.getImageUrl())
                            .build())
                    .putAllData(requestDto.getData() != null ? requestDto.getData() : Map.of())
                    .setCondition(condition)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            return ResponseEntity.ok(
                    SendNotificationResponseDto.builder()
                            .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                            .statusCode(200)
                            .target("Condition")
                            .message("Successfully sent message: " + response)
                            .build()
            );
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }

    private ResponseEntity<?> sendNotificationToTopic(String topic, SendNotificationRequestDto requestDto) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(requestDto.getTitle())
                            .setBody(requestDto.getBody())
                            .setImage(requestDto.getImageUrl())
                            .build())
                    .putAllData(requestDto.getData() != null ? requestDto.getData() : Map.of())
                    .setTopic(topic)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            return ResponseEntity.ok(
                    SendNotificationResponseDto.builder()
                            .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                            .statusCode(200)
                            .target("Topic")
                            .message("Successfully sent message: " + response)
                            .build()
            );
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }

    private ResponseEntity<?> sendNotificationToDeviceGroup(String deviceGroupKey, SendNotificationRequestDto requestDto) {
        try {
            ResponseEntity<?> responseEntity = new FcmUtility().sendNotificationWithRestClient(requestDto);
            return ResponseEntity.ok(
                    SendNotificationResponseDto.builder()
                            .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                            .statusCode(200)
                            .target("Devices Group")
                            .message("Successfully sent message: " + responseEntity)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }

    private ResponseEntity<?> sendNotificationToDevices(List<String> deviceTokens, SendNotificationRequestDto requestDto) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(requestDto.getTitle())
                            .setBody(requestDto.getBody())
                            .setImage(requestDto.getImageUrl())
                            .build())
                    .putAllData(requestDto.getData() != null ? requestDto.getData() : Map.of())
                    .addAllTokens(deviceTokens)
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            List<String> failedTokens = new ArrayList<>();
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        failedTokens.add(deviceTokens.get(i));
                    }
                }
            }

            return ResponseEntity.ok(

                    SendNotificationResponseDto.builder()
                            .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                            .statusCode(200)
                            .target("Devices")
                            .message(
                                    Map.of(
                                            "successCount", response.getSuccessCount(),
                                            "failureCount", response.getFailureCount(),
                                            "failedTokens", failedTokens
                                    ))
                            .build()
            );
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }


    private boolean hasMoreThanOneTarget(SendNotificationRequestDto dto) {
        int count = 0;
        if (dto.getDeviceTokens() != null && !dto.getDeviceTokens().isEmpty()) count++;
        if (dto.getDeviceGroupKey() != null && !dto.getDeviceGroupKey().isBlank()) count++;
        if (dto.getTopic() != null && !dto.getTopic().isBlank()) count++;
        if (dto.getCondition() != null && !dto.getCondition().isBlank()) count++;
        return count > 1;
    }

    @Override
    public ResponseEntity<?> sendBatchNotification(SendBatchNotificationRequestDto batchRequestDto) {

        for (SendNotificationRequestDto dto : batchRequestDto.getNotifications()) {
            if (hasMoreThanOneTarget(dto)) {
                return ResponseEntity.badRequest().body("Only one of deviceTokens, deviceGroupKey, topic, or condition should be provided.");
            }
        }
        try {
            // Flatten the list of lists into a single list of messages
            List<Message> messages = batchRequestDto.getNotifications().stream()
                    .flatMap(dto -> createMessagesFromNotificationRequest(dto).stream())
                    .collect(Collectors.toList());

            if (messages.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid notification targets found.");
            }
            // Send all messages as a batch
            BatchResponse response = FirebaseMessaging.getInstance().sendAll(messages);

            List<Message> failedTokens = new ArrayList<>();
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        failedTokens.add(messages.get(i));
                    }
                }
            }

            return ResponseEntity.ok(

                    SendNotificationResponseDto.builder()
                            .status(AppConstantsNotificationAndEmailService.SUCCESS_API_STATUS)
                            .statusCode(200)
                            .target("Batch")
                            .message(
                                    Map.of(
                                            "successCount", response.getSuccessCount(),
                                            "failureCount", response.getFailureCount(),
                                            "failedTokens", failedTokens
                                    ))
                            .build()
            );
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.internalServerError().body(AppConstantsNotificationAndEmailService.errorDto(e));
        }
    }

    // Helper method to map SendNotificationRequestDto to Firebase Message
    private List<Message> createMessagesFromNotificationRequest(SendNotificationRequestDto requestDto) {
        List<Message> messages = new ArrayList<>();

        Notification notification = Notification.builder()
                .setTitle(requestDto.getTitle())
                .setBody(requestDto.getBody())
                .setImage(requestDto.getImageUrl())
                .build();

        Map<String, String> data = requestDto.getData() != null ? requestDto.getData() : new HashMap<>();

        // Case 1: Send to individual device tokens
        if (requestDto.getDeviceTokens() != null && !requestDto.getDeviceTokens().isEmpty()) {
            for (String token : requestDto.getDeviceTokens()) {
                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(notification)
                        .putAllData(data)
                        .build();
                messages.add(message);
            }
        }

        // Case 2: Device group (via a topic - notification key)
        else if (requestDto.getDeviceGroupKey() != null) {
            Message message = Message.builder()
                    .setTopic(requestDto.getDeviceGroupKey())
                    .setNotification(notification)
                    .putAllData(data)
                    .build();
            messages.add(message);
        }

        // Case 3: Topic
        else if (requestDto.getTopic() != null) {
            Message message = Message.builder()
                    .setTopic(requestDto.getTopic())
                    .setNotification(notification)
                    .putAllData(data)
                    .build();
            messages.add(message);
        }

        // Case 4: Condition
        else if (requestDto.getCondition() != null) {
            Message message = Message.builder()
                    .setCondition(requestDto.getCondition())
                    .setNotification(notification)
                    .putAllData(data)
                    .build();
            messages.add(message);
        }

        return messages;
    }

}
