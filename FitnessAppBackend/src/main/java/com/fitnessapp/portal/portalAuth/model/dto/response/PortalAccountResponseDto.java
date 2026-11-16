package com.fitnessapp.portal.portalAuth.model.dto.response;

import com.fitnessapp.portal.portalAuth.model.enums.GenderPortal;
import com.fitnessapp.portal.portalAuth.model.enums.UserRole;

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
