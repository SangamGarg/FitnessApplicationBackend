package com.fitnessapp.notification.dto.response;

import com.fitnessapp.appAuth.model.entity.UserEntity;

public record AppUserForNotificationResponseDto(
        String name,
        String email
) {
    public static AppUserForNotificationResponseDto from(UserEntity e) {
        return new AppUserForNotificationResponseDto(
                e.getUserDetailsEntity().getName(), e.getEmail());
    }
}
