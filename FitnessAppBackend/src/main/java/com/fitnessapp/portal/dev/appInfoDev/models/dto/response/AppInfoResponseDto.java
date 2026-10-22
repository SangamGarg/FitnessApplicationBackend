package com.fitnessapp.portal.dev.appInfoDev.models.dto.response;

import com.fitnessapp.portal.dev.appInfoDev.models.entity.AppInfoAboutUs;
import com.fitnessapp.portal.dev.appInfoDev.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.portal.dev.appInfoDev.models.entity.AppInfoTermsAndCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInfoResponseDto {
    private String version;
    private String createdAt;
    private AppInfoAboutUs aboutUs;
    private AppInfoPrivacyPolicy privacyPolicy;
    private AppInfoTermsAndCondition termsAndCondition;
    private List<AppInfoLinksResponseDto> links;
    private List<AppInfoFaqsResponseDto> faqs;
}