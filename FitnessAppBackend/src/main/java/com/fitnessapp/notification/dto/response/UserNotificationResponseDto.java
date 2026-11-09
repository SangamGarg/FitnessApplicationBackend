package com.fitnessapp.notification.dto.response;

import com.fitnessapp.notification.model.entity.UserNotificationEntity;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class UserNotificationResponseDto {
    UUID id;
    String title;
    String body;
    String imageUrl;
    Map<String, String> data;
    Instant createdAt;
    Instant readAt;
    boolean read;

    public static UserNotificationResponseDto from(UserNotificationEntity entity) {
        return UserNotificationResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .imageUrl(entity.getImageUrl())
                .data(entity.getData())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .read(entity.getReadAt() != null)
                .build();
    }
}
