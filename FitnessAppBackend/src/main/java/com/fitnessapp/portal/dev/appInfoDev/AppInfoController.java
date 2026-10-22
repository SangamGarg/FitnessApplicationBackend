package com.fitnessapp.portal.dev.appInfoDev;

import com.fitnessapp.portal.dev.appInfoDev.models.dto.request.*;
import com.fitnessapp.portal.dev.appInfoDev.models.dto.response.AppInfoFaqsResponseDto;
import com.fitnessapp.portal.dev.appInfoDev.models.dto.response.AppInfoLinksResponseDto;
import com.fitnessapp.portal.dev.appInfoDev.models.dto.response.AppInfoResponseDto;
import com.fitnessapp.portal.dev.appInfoDev.models.entity.AppInfoAboutUs;
import com.fitnessapp.portal.dev.appInfoDev.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.portal.dev.appInfoDev.models.entity.AppInfoTermsAndCondition;
import com.fitnessapp.portal.dev.shared.DevUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(DevUtils.API_PREFIX_APP_INFO)
@Tag(name = "App Info", description = "Endpoints for managing portal app info, policies, links, and FAQs")
@SecurityRequirement(name = "bearerAuth")
public class AppInfoController {

    private final AppInfoService appInfoService;

    @Operation(summary = "Upload About Us", description = "Saves or overwrites About Us content.")
    @PostMapping("/upload/about")
    public ResponseEntity<Map<String, String>> uploadAboutUs(@Valid @RequestBody AppInfoAboutUsRequestDto requestDto) {
        log.info("REST request to upload About Us");
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadAboutUs(requestDto));
    }

    @Operation(summary = "Patch About Us", description = "Updates About Us content.")
    @PatchMapping("/patch/about")
    public ResponseEntity<Map<String, String>> patchAboutUs(@Valid @RequestBody AppInfoAboutUsRequestDto requestDto) {
        log.info("REST request to patch About Us");
        return ResponseEntity.ok(appInfoService.patchAboutUs(requestDto));
    }

    @Operation(summary = "Upload Privacy Policy", description = "Saves or overwrites Privacy Policy content.")
    @PostMapping("/upload/privacy-policy")
    public ResponseEntity<Map<String, String>> uploadPrivacyPolicy(@Valid @RequestBody AppInfoPrivacyPolicyRequestDto requestDto) {
        log.info("REST request to upload Privacy Policy");
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadPrivacyPolicy(requestDto));
    }

    @Operation(summary = "Patch Privacy Policy", description = "Updates Privacy Policy content.")
    @PatchMapping("/patch/privacy-policy")
    public ResponseEntity<Map<String, String>> patchPrivacyPolicy(@Valid @RequestBody AppInfoPrivacyPolicyRequestDto requestDto) {
        log.info("REST request to patch Privacy Policy");
        return ResponseEntity.ok(appInfoService.patchPrivacyPolicy(requestDto));
    }

    @Operation(summary = "Upload Terms and Conditions", description = "Saves or overwrites Terms & Conditions content.")
    @PostMapping("/upload/terms-condition")
    public ResponseEntity<Map<String, String>> uploadTermsAndConditions(@Valid @RequestBody AppInfoTermsAndConditionRequestDto requestDto) {
        log.info("REST request to upload Terms and Conditions");
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadTermsAndCondition(requestDto));
    }

    @Operation(summary = "Patch Terms and Conditions", description = "Updates Terms & Conditions content.")
    @PatchMapping("/patch/terms-condition")
    public ResponseEntity<Map<String, String>> patchTermsAndConditions(@Valid @RequestBody AppInfoTermsAndConditionRequestDto requestDto) {
        log.info("REST request to patch Terms and Conditions");
        return ResponseEntity.ok(appInfoService.patchTermsAndCondition(requestDto));
    }

    @Operation(summary = "Upload App Links URL List", description = "Overwrites the existing collection of app links.")
    @PostMapping("/upload/app-links")
    public ResponseEntity<Map<String, String>> uploadAppLinksUrl(
             @NotEmpty(message = "Links list cannot be empty") @RequestBody List<AppInfoLinksRequestDto> requestDto
    ) {
        log.info("REST request to upload {} app links", requestDto.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadAppLinksUrl(requestDto));
    }

    @Operation(summary = "Patch App Link URL", description = "Updates a single link matching the specified title.")
    @PatchMapping("/patch/app-links")
    public ResponseEntity<Map<String, String>> patchAppLinksUrl(@Valid @RequestBody AppInfoLinksRequestDto requestDto) {
        log.info("REST request to patch app link: [{}]", requestDto.getTitle());
        return ResponseEntity.ok(appInfoService.patchAppLinksUrl(requestDto));
    }

    @Operation(summary = "Upload App FAQs List", description = "Overwrites the existing collection of FAQs.")
    @PostMapping("/upload/app-faqs")
    public ResponseEntity<Map<String, String>> uploadAppFaqs(
             @NotEmpty(message = "FAQ list cannot be empty") @RequestBody List<AppInfoFaqsRequestDto> requestDto
    ) {
        log.info("REST request to upload {} FAQs", requestDto.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(appInfoService.uploadAppFqs(requestDto));
    }

    @Operation(summary = "Patch App FAQ by ID", description = "Updates the question and answer for a given FAQ ID.")
    @PatchMapping("/patch/app-faqs/{id}")
    public ResponseEntity<Map<String, String>> patchAppFaqs(
            @Valid @RequestBody AppInfoFaqsRequestDto requestDto,
            @PathVariable Long id
    ) {
        log.info("REST request to patch FAQ with ID: [{}]", id);
        return ResponseEntity.ok(appInfoService.patchAppFaqs(requestDto, id));
    }
    @Operation(summary = "Get Complete App Info", description = "Retrieves all app policies, links, and FAQs in a single payload.")
    @GetMapping
    public ResponseEntity<AppInfoResponseDto> getCompleteAppInfo() {
        log.debug("REST request to fetch all App Info");
        return ResponseEntity.ok(appInfoService.getCompleteAppInfo());
    }

    @Operation(summary = "Get About Us", description = "Retrieves About Us content and metadata.")
    @GetMapping("/about")
    public ResponseEntity<AppInfoAboutUs> getAboutUs() {
        log.debug("REST request to fetch About Us");
        return ResponseEntity.ok(appInfoService.getAboutUs());
    }

    @Operation(summary = "Get Privacy Policy", description = "Retrieves Privacy Policy content and metadata.")
    @GetMapping("/privacy-policy")
    public ResponseEntity<AppInfoPrivacyPolicy> getPrivacyPolicy() {
        log.debug("REST request to fetch Privacy Policy");
        return ResponseEntity.ok(appInfoService.getPrivacyPolicy());
    }

    @Operation(summary = "Get Terms and Conditions", description = "Retrieves Terms and Conditions content and metadata.")
    @GetMapping("/terms-condition")
    public ResponseEntity<AppInfoTermsAndCondition> getTermsAndConditions() {
        log.debug("REST request to fetch Terms and Conditions");
        return ResponseEntity.ok(appInfoService.getTermsAndConditions());
    }

    @Operation(summary = "Get App Links", description = "Retrieves all registered application links.")
    @GetMapping("/app-links")
    public ResponseEntity<List<AppInfoLinksResponseDto>> getAppLinks() {
        log.debug("REST request to fetch App Links");
        return ResponseEntity.ok(appInfoService.getAppLinks());
    }

    @Operation(summary = "Get App FAQs", description = "Retrieves all frequently asked questions.")
    @GetMapping("/app-faqs")
    public ResponseEntity<List<AppInfoFaqsResponseDto>> getAppFaqs() {
        log.debug("REST request to fetch FAQs");
        return ResponseEntity.ok(appInfoService.getAppFaqs());
    }
}