package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.controller;

import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.*;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.service.AppInfoService;
import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_APP_INFO)
public class AppInfoController {

    private final AppInfoService appInfoService;

    public AppInfoController(AppInfoService appInfoService) {
        this.appInfoService = appInfoService;
    }

    @PostMapping("upload/about")
    public ResponseEntity<?> uploadAboutUs(@RequestBody @Valid AppInfoAboutUsRequestDto requestDto) {
        return appInfoService.uploadAboutUs(requestDto);
    }

    @PatchMapping("patch/about")
    public ResponseEntity<?> patchAboutUs(@RequestBody @Valid AppInfoAboutUsRequestDto requestDto) {
        return appInfoService.patchAboutUs(requestDto);
    }

    @PostMapping("upload/privacy-policy")
    public ResponseEntity<?> uploadPrivacyPolicy(@RequestBody @Valid AppInfoPrivacyPolicyRequestDto requestDto) {
        return appInfoService.uploadPrivacyPolicy(requestDto);
    }

    @PatchMapping("patch/privacy-policy")
    public ResponseEntity<?> patchPrivacyPolicy(@RequestBody @Valid AppInfoPrivacyPolicyRequestDto requestDto) {
        return appInfoService.patchPrivacyPolicy(requestDto);
    }

    @PostMapping("upload/terms-condition")
    public ResponseEntity<?> uploadTermsAndConditions(@RequestBody @Valid AppInfoTermsAndConditionRequestDto requestDto) {
        return appInfoService.uploadTermsAndCondition(requestDto);
    }

    @PatchMapping("patch/terms-condition")
    public ResponseEntity<?> patchTermsAndConditions(@RequestBody @Valid AppInfoTermsAndConditionRequestDto requestDto) {
        return appInfoService.patchTermsAndCondition(requestDto);
    }


    @PostMapping("upload/app-links")
    public ResponseEntity<?> uploadAppLinksUrl(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<AppInfoLinksRequestDto> requestDto) {
        return appInfoService.uploadAppLinksUrl(requestDto);
    }

    @PatchMapping("patch/app-links")
    public ResponseEntity<?> patchAppLinksUrl(@RequestBody @Valid AppInfoLinksRequestDto requestDto) {
        return appInfoService.patchAppLinksUrl(requestDto);
    }


    @PostMapping("upload/app-faqs")
    public ResponseEntity<?> uploadAppFqs(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<AppInfoFaqsRequestDto> requestDto) {
        return appInfoService.uploadAppFqs(requestDto);
    }

    @PatchMapping("patch/app-faqs/{id}")
    public ResponseEntity<?> patchAppFaqs(@RequestBody @Valid AppInfoFaqsRequestDto requestDto, @PathVariable Long id) {
        return appInfoService.patchAppFaqs(requestDto, id);
    }
}
