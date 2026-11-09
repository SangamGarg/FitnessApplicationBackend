package com.fitnessapp.support.portalSupport.service;

import com.fitnessapp.support.portalSupport.models.dto.request.*;
import com.fitnessapp.support.portalSupport.models.dto.response.AppInfoFaqsResponseDto;
import com.fitnessapp.support.portalSupport.models.dto.response.AppInfoLinksResponseDto;
import com.fitnessapp.support.portalSupport.models.dto.response.AppInfoResponseDto;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;

import java.util.List;
import java.util.Map;

public interface AppInfoService {
    // --- READ ENDPOINTS ---
    AppInfoResponseDto getCompleteAppInfo();

    AppInfoAboutUs getAboutUs();

    AppInfoPrivacyPolicy getPrivacyPolicy();

    AppInfoTermsAndCondition getTermsAndConditions();

    List<AppInfoLinksResponseDto> getAppLinks();

    List<AppInfoFaqsResponseDto> getAppFaqs();

    // --- MUTATION ENDPOINTS ---
    Map<String, String> uploadAboutUs(AppInfoAboutUsRequestDto dto);

    Map<String, String> patchAboutUs(AppInfoAboutUsRequestDto dto);

    Map<String, String> uploadPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto);

    Map<String, String> patchPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto);

    Map<String, String> uploadTermsAndCondition(AppInfoTermsAndConditionRequestDto dto);

    Map<String, String> patchTermsAndCondition(AppInfoTermsAndConditionRequestDto dto);

    Map<String, String> uploadAppLinksUrl(List<AppInfoLinksRequestDto> dtoList);

    Map<String, String> patchAppLinksUrl(AppInfoLinksRequestDto dto);

    Map<String, String> uploadAppFqs(List<AppInfoFaqsRequestDto> dtoList);

    Map<String, String> patchAppFaqs(AppInfoFaqsRequestDto dto, Long id);
}