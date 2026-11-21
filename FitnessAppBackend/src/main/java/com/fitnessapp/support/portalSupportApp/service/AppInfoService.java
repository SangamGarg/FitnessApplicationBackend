package com.fitnessapp.support.portalSupportApp.service;

import com.fitnessapp.support.models.dtos.SupportFaqCategoryDto;
import com.fitnessapp.support.portalSupportApp.models.dto.request.*;
import com.fitnessapp.support.portalSupportApp.models.dto.response.PortalFaqResponseDto;
import com.fitnessapp.support.portalSupportApp.models.dto.response.PortalAppInfoResponseDto;

import java.util.List;
import java.util.Map;

public interface AppInfoService {
    // --- MUTATION ENDPOINTS ---
    Map<String, String> uploadAboutUs(AppInfoAboutUsRequestDto dto);

    Map<String, String> patchAboutUs(AppInfoAboutUsRequestDto dto);

    Map<String, String> uploadPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto);

    Map<String, String> patchPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto);

    Map<String, String> uploadTermsAndCondition(AppInfoTermsAndConditionRequestDto dto);

    Map<String, String> patchTermsAndCondition(AppInfoTermsAndConditionRequestDto dto);

    Map<String, String> uploadAppFqs(List<AppInfoFaqsRequestDto> dtoList);

    Map<String, String> patchAppFaqs(AppInfoFaqPatchRequestDto dto,
                                     Long id);
    SupportFaqCategoryDto createCategory(
            SupportFaqCategoryRequestDto requestDto
    );

    PortalAppInfoResponseDto getInfoForPortal();

    List<PortalFaqResponseDto> getAllFaqsForPortal();
}