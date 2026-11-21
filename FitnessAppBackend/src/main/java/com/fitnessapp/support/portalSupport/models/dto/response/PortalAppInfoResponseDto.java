package com.fitnessapp.support.portalSupport.models.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PortalAppInfoResponseDto(
        AppInfoResponseDto aboutUs,
        AppInfoResponseDto termsAndConditions,
        AppInfoResponseDto privacyPolicy
        ) {

    public record AppInfoResponseDto(String content,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt,
                                     Long version,
                                     String updatedBy) {
    }
}