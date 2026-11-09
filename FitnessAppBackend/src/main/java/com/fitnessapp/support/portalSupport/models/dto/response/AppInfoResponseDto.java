package com.fitnessapp.support.portalSupport.models.dto.response;

import com.fitnessapp.followUsLinks.FollowUsLinksResponseDto;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;
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
    private List<FollowUsLinksResponseDto> links;
    private List<AppInfoFaqsResponseDto> faqs;
}