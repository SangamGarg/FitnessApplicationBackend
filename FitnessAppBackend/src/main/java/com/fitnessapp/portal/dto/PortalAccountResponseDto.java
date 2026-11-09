package com.fitnessapp.portal.dto;

import com.fitnessapp.portal.common.enums.Gender;
import com.fitnessapp.portal.common.enums.UserRole;

import java.time.Instant;

public record PortalAccountResponseDto(
        Long id,
        String name,
        String email,
        String mobileNumber,
        Gender gender,
        String address,
        UserRole role,
        boolean enabled,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
