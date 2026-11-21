package com.fitnessapp.support.portalSupportApp.models.dto.response;

import com.fitnessapp.support.portalSupportApp.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupportApp.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupportApp.models.entity.AppInfoTermsAndCondition;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PortalAppInfoResponseDto(
        AppInfoResponseDto aboutUs,
        AppInfoResponseDto termsAndConditions,
        AppInfoResponseDto privacyPolicy
        ) {

    public static PortalAppInfoResponseDto from(
            AppInfoAboutUs aboutUs,
            AppInfoTermsAndCondition termsAndConditions,
            AppInfoPrivacyPolicy privacyPolicy
    ) {
        return PortalAppInfoResponseDto.builder()
                .aboutUs(AppInfoResponseDto.from(
                        aboutUs == null ? null : aboutUs.getContent(),
                        aboutUs == null ? null : aboutUs.getCreatedAt(),
                        aboutUs == null ? null : aboutUs.getUpdatedAt(),
                        aboutUs == null ? null : aboutUs.getVersion(),
                        aboutUs == null ? null : aboutUs.getUpdatedBy()
                ))
                .termsAndConditions(AppInfoResponseDto.from(
                        termsAndConditions == null ? null : termsAndConditions.getContent(),
                        termsAndConditions == null ? null : termsAndConditions.getCreatedAt(),
                        termsAndConditions == null ? null : termsAndConditions.getUpdatedAt(),
                        termsAndConditions == null ? null : termsAndConditions.getVersion(),
                        termsAndConditions == null ? null : termsAndConditions.getUpdatedBy()
                ))
                .privacyPolicy(AppInfoResponseDto.from(
                        privacyPolicy == null ? null : privacyPolicy.getContent(),
                        privacyPolicy == null ? null : privacyPolicy.getCreatedAt(),
                        privacyPolicy == null ? null : privacyPolicy.getUpdatedAt(),
                        privacyPolicy == null ? null : privacyPolicy.getVersion(),
                        privacyPolicy == null ? null : privacyPolicy.getUpdatedBy()
                ))
                .build();
    }

    public record AppInfoResponseDto(String content,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt,
                                     Long version,
                                     String updatedBy) {
        private static AppInfoResponseDto from(
                String content,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                Long version,
                String updatedBy
        ) {
            if (content == null) {
                return null;
            }
            return new AppInfoResponseDto(content, createdAt, updatedAt, version, updatedBy);
        }
    }
}