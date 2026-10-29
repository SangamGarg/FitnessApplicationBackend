package com.fitnessapp.portal.dto;

import com.fitnessapp.appAuth.model.entity.MobileAccountStatus;

public record MobileUserAdminResponseDto(
        String id,
        String email,
        String name,
        String gender,
        Integer age,
        String profileImageUrl,
        Float heightCm,
        Float weightKg,
        String activityLevel,
        String goal,
        MobileAccountStatus status
) {
}
