package com.fitnessapp.support.portalSupport.controller;

import com.fitnessapp.support.models.dtos.SupportFaqCategoryDto;
import com.fitnessapp.support.portalSupport.models.dto.request.*;
import com.fitnessapp.support.portalSupport.service.AppInfoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_SUPPORT_ENDPOINT;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(PORTAL_SUPPORT_ENDPOINT)
public class PortalAppInfoController {

    private final AppInfoService appInfoService;

    @PostMapping("/upload/about")
    public ResponseEntity<Map<String, String>> uploadAboutUs(@Valid @RequestBody AppInfoAboutUsRequestDto requestDto) {
        log.info("REST request to upload About Us");
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadAboutUs(requestDto));
    }

    @PatchMapping("/patch/about")
    public ResponseEntity<Map<String, String>> patchAboutUs(@Valid @RequestBody AppInfoAboutUsRequestDto requestDto) {
        log.info("REST request to patch About Us");
        return ResponseEntity.ok(appInfoService.patchAboutUs(requestDto));
    }

    @PostMapping("/upload/privacy-policy")
    public ResponseEntity<Map<String, String>> uploadPrivacyPolicy(@Valid @RequestBody AppInfoPrivacyPolicyRequestDto requestDto) {
        log.info("REST request to upload Privacy Policy");
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadPrivacyPolicy(requestDto));
    }

    @PatchMapping("/patch/privacy-policy")
    public ResponseEntity<Map<String, String>> patchPrivacyPolicy(@Valid @RequestBody AppInfoPrivacyPolicyRequestDto requestDto) {
        log.info("REST request to patch Privacy Policy");
        return ResponseEntity.ok(appInfoService.patchPrivacyPolicy(requestDto));
    }

    @PostMapping("/upload/terms-condition")
    public ResponseEntity<Map<String, String>> uploadTermsAndConditions(@Valid @RequestBody AppInfoTermsAndConditionRequestDto requestDto) {
        log.info("REST request to upload Terms and Conditions");
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadTermsAndCondition(requestDto));
    }

    @PatchMapping("/patch/terms-condition")
    public ResponseEntity<Map<String, String>> patchTermsAndConditions(@Valid @RequestBody AppInfoTermsAndConditionRequestDto requestDto) {
        log.info("REST request to patch Terms and Conditions");
        return ResponseEntity.ok(appInfoService.patchTermsAndCondition(requestDto));
    }


    @PostMapping("/upload/faqs")
    public ResponseEntity<Map<String, String>> uploadAppFaqs(
            @NotEmpty(message = "FAQ list cannot be empty") @RequestBody List<AppInfoFaqsRequestDto> requestDto
    ) {
        log.info("REST request to upload {} FAQs", requestDto.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadAppFqs(requestDto));
    }

    @PatchMapping("/patch/faqs/{id}")
    public ResponseEntity<Map<String, String>> patchAppFaqs(
            @Valid @RequestBody AppInfoFaqPatchRequestDto requestDto,
            @PathVariable Long id
    ) {
        log.info("REST request to patch FAQ with ID: [{}]", id);
        return ResponseEntity.ok(appInfoService.patchAppFaqs(requestDto, id));
    }


    @PostMapping("/upload/faqs/category")
    public ResponseEntity<SupportFaqCategoryDto> uploadAppFaqsCategory(@Valid @RequestBody SupportFaqCategoryRequestDto requestDto
    ) {
        log.info("REST request to upload Category FAQs");
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.createCategory(requestDto));
    }


}