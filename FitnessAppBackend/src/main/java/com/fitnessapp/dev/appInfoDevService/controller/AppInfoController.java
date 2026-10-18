package com.fitnessapp.dev.appInfoDevService.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.fitnessapp.dev.appInfoDevService.models.dto.*;
import com.fitnessapp.dev.appInfoDevService.service.AppInfoService;
import com.fitnessapp.dev.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(DevAndTestingServiceUtility.API_PREFIX_APP_INFO)
@Tag(name = "App Info", description = "Endpoints for app info")
public class AppInfoController {

    private final AppInfoService appInfoService;

    public AppInfoController(AppInfoService appInfoService) {
        this.appInfoService = appInfoService;
    }

    @Operation(summary = "upload About Us", description = """
        Executes the upload about us operation.
        """)
    @PostMapping("upload/about")
    public ResponseEntity<?> uploadAboutUs(@RequestBody @Valid AppInfoAboutUsRequestDto requestDto) {
        return appInfoService.uploadAboutUs(requestDto);
    }

    @Operation(summary = "patch About Us", description = """
        Executes the patch about us operation.
        """)
    @PatchMapping("patch/about")
    public ResponseEntity<?> patchAboutUs(@RequestBody @Valid AppInfoAboutUsRequestDto requestDto) {
        return appInfoService.patchAboutUs(requestDto);
    }

    @Operation(summary = "upload Privacy Policy", description = """
        Executes the upload privacy policy operation.
        """)
    @PostMapping("upload/privacy-policy")
    public ResponseEntity<?> uploadPrivacyPolicy(@RequestBody @Valid AppInfoPrivacyPolicyRequestDto requestDto) {
        return appInfoService.uploadPrivacyPolicy(requestDto);
    }

    @Operation(summary = "patch Privacy Policy", description = """
        Executes the patch privacy policy operation.
        """)
    @PatchMapping("patch/privacy-policy")
    public ResponseEntity<?> patchPrivacyPolicy(@RequestBody @Valid AppInfoPrivacyPolicyRequestDto requestDto) {
        return appInfoService.patchPrivacyPolicy(requestDto);
    }

    @Operation(summary = "upload Terms And Conditions", description = """
        Executes the upload terms and conditions operation.
        """)
    @PostMapping("upload/terms-condition")
    public ResponseEntity<?> uploadTermsAndConditions(@RequestBody @Valid AppInfoTermsAndConditionRequestDto requestDto) {
        return appInfoService.uploadTermsAndCondition(requestDto);
    }

    @Operation(summary = "patch Terms And Conditions", description = """
        Executes the patch terms and conditions operation.
        """)
    @PatchMapping("patch/terms-condition")
    public ResponseEntity<?> patchTermsAndConditions(@RequestBody @Valid AppInfoTermsAndConditionRequestDto requestDto) {
        return appInfoService.patchTermsAndCondition(requestDto);
    }


    @Operation(summary = "upload App Links Url", description = """
        Executes the upload app links url operation.
        """)
    @PostMapping("upload/app-links")
    public ResponseEntity<?> uploadAppLinksUrl(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<AppInfoLinksRequestDto> requestDto) {
        return appInfoService.uploadAppLinksUrl(requestDto);
    }

    @Operation(summary = "patch App Links Url", description = """
        Executes the patch app links url operation.
        """)
    @PatchMapping("patch/app-links")
    public ResponseEntity<?> patchAppLinksUrl(@RequestBody @Valid AppInfoLinksRequestDto requestDto) {
        return appInfoService.patchAppLinksUrl(requestDto);
    }


    @Operation(summary = "upload App Fqs", description = """
        Executes the upload app fqs operation.
        """)
    @PostMapping("upload/app-faqs")
    public ResponseEntity<?> uploadAppFqs(@RequestBody @Valid @NotEmpty(message = "List Cannot be empty") List<AppInfoFaqsRequestDto> requestDto) {
        return appInfoService.uploadAppFqs(requestDto);
    }

    @Operation(summary = "patch App Faqs", description = """
        Executes the patch app faqs operation.
        """)
    @PatchMapping("patch/app-faqs/{id}")
    public ResponseEntity<?> patchAppFaqs(@RequestBody @Valid AppInfoFaqsRequestDto requestDto, @PathVariable Long id) {
        return appInfoService.patchAppFaqs(requestDto, id);
    }


}
