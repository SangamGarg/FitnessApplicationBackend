package com.fitnessapp.support.portalSupport.service;

import com.fitnessapp.support.portalSupport.models.dto.request.*;

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
}