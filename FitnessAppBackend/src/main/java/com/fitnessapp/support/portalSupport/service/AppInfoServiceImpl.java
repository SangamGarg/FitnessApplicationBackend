package com.fitnessapp.support.portalSupport.service;

import com.fitnessapp.followUsLinks.FollowUsLinksResponseDto;
import com.fitnessapp.support.portalSupport.models.dto.request.AppInfoAboutUsRequestDto;
import com.fitnessapp.support.portalSupport.models.dto.request.AppInfoFaqsRequestDto;
import com.fitnessapp.support.portalSupport.models.dto.request.AppInfoPrivacyPolicyRequestDto;
import com.fitnessapp.support.portalSupport.models.dto.request.AppInfoTermsAndConditionRequestDto;
import com.fitnessapp.support.portalSupport.models.dto.response.AppInfoResponseDto;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoEntity;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;
import com.fitnessapp.support.portalSupport.repo.AppInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppInfoServiceImpl implements AppInfoService {

    private static final Long APP_INFO_SINGLETON_ID = 1L;

    private final AppInfoRepository appInfoRepository;
    private final com.fitnessapp.followUsLinks.FollowUsService followUsService;

    @Value("${version.app:1.0.0}")
    private String version;

    /**
     * Used only by write operations. Read-only methods must never create database rows.
     * The singleton ID is fixed so the app-info record remains a single logical record.
     */
    private AppInfoEntity getOrCreateAppInfoForWrite() {
        return appInfoRepository.findById(APP_INFO_SINGLETON_ID)
                .orElseGet(() -> {
                    log.info("AppInfo record not found. Initializing singleton record with ID {}", APP_INFO_SINGLETON_ID);
                    AppInfoEntity entity = AppInfoEntity.builder()
                            .id(APP_INFO_SINGLETON_ID)
                            .appVersion(version)
                            .build();
                    return appInfoRepository.save(entity);
                });
    }

    /** Read-only lookup: never inserts a row as a side effect of a GET request. */
    private AppInfoEntity getAppInfoForRead() {
        return appInfoRepository.findById(APP_INFO_SINGLETON_ID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "App information has not been initialized"));
    }

    @Override
    @Transactional
    public Map<String, String> uploadAboutUs(AppInfoAboutUsRequestDto dto) {
        log.info("Uploading About Us section");
        AppInfoEntity entity = getOrCreateAppInfoForWrite();
        entity.setAppInfoAboutUs(AppInfoAboutUs.builder()
                .content(dto.getContent())
                .lastUpdatedBy("System Admin") // Or get from SecurityContext if available
                .build());
        appInfoRepository.save(entity);
        log.info("About Us section saved successfully");
        return Map.of("message", "About Us uploaded successfully");
    }

    @Override
    @Transactional
    public Map<String, String> patchAboutUs(AppInfoAboutUsRequestDto dto) {
        return uploadAboutUs(dto);
    }

    @Override
    @Transactional
    public Map<String, String> uploadPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto) {
        log.info("Uploading Privacy Policy section");
        AppInfoEntity entity = getOrCreateAppInfoForWrite();
        entity.setAppInfoPrivacyPolicy(AppInfoPrivacyPolicy.builder()
                .content(dto.getContent())
                .lastUpdatedBy("System Admin")
                .build());
        appInfoRepository.save(entity);
        log.info("Privacy Policy section saved successfully");
        return Map.of("message", "Privacy Policy uploaded successfully");
    }

    @Override
    @Transactional
    public Map<String, String> patchPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto) {
        return uploadPrivacyPolicy(dto);
    }

    @Override
    @Transactional
    public Map<String, String> uploadTermsAndCondition(AppInfoTermsAndConditionRequestDto dto) {
        log.info("Uploading Terms and Conditions section");
        AppInfoEntity entity = getOrCreateAppInfoForWrite();
        entity.setAppInfoTermsAndCondition(AppInfoTermsAndCondition.builder()
                .content(dto.getContent())
                .lastUpdatedBy("System Admin")
                .build());
        appInfoRepository.save(entity);
        log.info("Terms and Conditions section saved successfully");
        return Map.of("message", "Terms and Conditions uploaded successfully");
    }

    @Override
    @Transactional
    public Map<String, String> patchTermsAndCondition(AppInfoTermsAndConditionRequestDto dto) {
        return uploadTermsAndCondition(dto);
    }

    @Override
    public Map<String, String> uploadAppFqs(List<AppInfoFaqsRequestDto> dtoList) {
        return Map.of();
    }


    @Override
    @Transactional
    public Map<String, String> patchAppFaqs(AppInfoFaqsRequestDto dto, Long id) {
        //TODO
//        log.info("Patching FAQ with ID: [{}]", id);
//        AppInfoEntity entity = getOrCreateAppInfoForWrite();
//
//        AppInfoFaqs targetFaq = entity.getAppInfoFaqs().stream()
//                .filter(faq -> Objects.equals(faq.getId(), id))
//                .findFirst()
//                .orElseThrow(() -> {
//                    log.warn("FAQ with ID [{}] not found", id);
//                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "FAQ with ID " + id + " not found");
//                });
//
//        targetFaq.setQuestion(dto.getQuestion().trim());
//        targetFaq.setAnswer(dto.getAnswer().trim());
//
//        appInfoRepository.save(entity);
//        log.info("FAQ [{}] updated successfully", id);
        return Map.of("message", "FAQ updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public AppInfoResponseDto getCompleteAppInfo() {
        log.debug("Fetching complete AppInfo dataset");
        AppInfoEntity entity = getAppInfoForRead();

        List<FollowUsLinksResponseDto> links = followUsService.getAppLinks();

        // TODO: FAQs
        // List<AppInfoFaqsResponseDto> faqs = entity.getAppInfoFaqs().stream()
        //         .map(f -> new AppInfoFaqsResponseDto(f.getId(), f.getQuestion(), f.getAnswer()))
        //         .toList();

        return AppInfoResponseDto.builder()
                .version(entity.getAppVersion())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)
                .aboutUs(entity.getAppInfoAboutUs())
                .privacyPolicy(entity.getAppInfoPrivacyPolicy())
                .termsAndCondition(entity.getAppInfoTermsAndCondition())
                .links(links)
                // .faqs(faqs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AppInfoAboutUs getAboutUs() {
        log.debug("Fetching About Us content");
        return getAppInfoForRead().getAppInfoAboutUs();
    }

    @Override
    @Transactional(readOnly = true)
    public AppInfoPrivacyPolicy getPrivacyPolicy() {
        log.debug("Fetching Privacy Policy content");
        return getAppInfoForRead().getAppInfoPrivacyPolicy();
    }

    @Override
    @Transactional(readOnly = true)
    public AppInfoTermsAndCondition getTermsAndConditions() {
        log.debug("Fetching Terms and Conditions content");
        return getAppInfoForRead().getAppInfoTermsAndCondition();
    }
}
