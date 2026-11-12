package com.fitnessapp.portal.dto;

import com.fitnessapp.portal.common.enums.GenderPortal;
import com.fitnessapp.portal.common.enums.UserRole;

import java.time.Instant;

public record PortalAccountResponseDto(
        Long id,
        String name,
        String email,
        String mobileNumber,
        GenderPortal genderPortal,
        String address,
        UserRole role,
        boolean enabled,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
