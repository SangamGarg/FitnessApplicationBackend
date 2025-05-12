package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.service;

import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.AppInfoAboutUsRequestDto;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.AppInfoPrivacyPolicyRequestDto;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.AppInfoTermsAndConditionRequestDto;
import org.springframework.http.ResponseEntity;

public interface AppInfoService {

    ResponseEntity<?> uploadAboutUs(AppInfoAboutUsRequestDto appInfoAboutUsRequestDto);

    ResponseEntity<?> uploadPrivacyPolicy(AppInfoPrivacyPolicyRequestDto appInfoPrivacyPolicyRequestDto);

    ResponseEntity<?> uploadTermsAndCondition(AppInfoTermsAndConditionRequestDto appInfoTermsAndConditionRequestDto);

    ResponseEntity<?> patchAboutUs(AppInfoAboutUsRequestDto appInfoAboutUsRequestDto);

    ResponseEntity<?> patchPrivacyPolicy(AppInfoPrivacyPolicyRequestDto appInfoPrivacyPolicyRequestDto);

    ResponseEntity<?> patchTermsAndCondition(AppInfoTermsAndConditionRequestDto appInfoTermsAndConditionRequestDto);

}