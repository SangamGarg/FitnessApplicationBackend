package com.fitnessapp.portal.dto;

import com.fitnessapp.appAuth.model.enums.ActivityLevel;
import com.fitnessapp.appAuth.model.enums.FitnessGoal;
import com.fitnessapp.appAuth.model.enums.Gender;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;

import java.math.BigDecimal;

public record MobileUserAdminResponseDto(
        String id,
        String email,
        String name,
        Gender gender,
        Integer age,
        String profileImageUrl,
        BigDecimal heightCm,
        BigDecimal weightKg,
        ActivityLevel activityLevel,
        FitnessGoal goal,
        MobileAccountStatus status
) {
}
