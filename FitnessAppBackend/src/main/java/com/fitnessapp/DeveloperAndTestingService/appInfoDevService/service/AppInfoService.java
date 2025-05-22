package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.service;

import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AppInfoService {

    ResponseEntity<?> uploadAboutUs(AppInfoAboutUsRequestDto appInfoAboutUsRequestDto);

    ResponseEntity<?> uploadPrivacyPolicy(AppInfoPrivacyPolicyRequestDto appInfoPrivacyPolicyRequestDto);

    ResponseEntity<?> uploadTermsAndCondition(AppInfoTermsAndConditionRequestDto appInfoTermsAndConditionRequestDto);

    ResponseEntity<?> patchAboutUs(AppInfoAboutUsRequestDto appInfoAboutUsRequestDto);

    ResponseEntity<?> patchPrivacyPolicy(AppInfoPrivacyPolicyRequestDto appInfoPrivacyPolicyRequestDto);

    ResponseEntity<?> patchTermsAndCondition(AppInfoTermsAndConditionRequestDto appInfoTermsAndConditionRequestDto);


    ResponseEntity<?> uploadAppLinksUrl(List<AppInfoLinksRequestDto> appInfoLinksRequestDto);


    ResponseEntity<?> patchAppLinksUrl(AppInfoLinksRequestDto appInfoLinksRequestDto);



    ResponseEntity<?> uploadAppFqs(List<AppInfoFaqsRequestDto> appInfoFaqsRequestDto);


    ResponseEntity<?> patchAppFaqs(AppInfoFaqsRequestDto appInfoFaqsRequestDto, Long id);

}