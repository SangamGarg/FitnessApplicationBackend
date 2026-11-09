
package com.fitnessapp.support.service;

import com.fitnessapp.support.models.dtos.response.AppInfoContentResponseDto;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;
import com.fitnessapp.support.portalSupport.repo.AppInfoAboutUsRepository;
import com.fitnessapp.support.portalSupport.repo.AppInfoPrivacyPolicyRepository;
import com.fitnessapp.support.portalSupport.repo.AppInfoTermsAndConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private static final Long CONTENT_ID = 1L;

    private final AppInfoAboutUsRepository aboutUsRepository;
    private final AppInfoPrivacyPolicyRepository privacyPolicyRepository;
    private final AppInfoTermsAndConditionRepository termsAndConditionRepository;

    // ---------------------------------------------------------
    // ABOUT US
    // ---------------------------------------------------------

    @Override
    public AppInfoContentResponseDto getAboutUs() {

        log.debug("Fetching About Us content");

        AppInfoAboutUs detail = aboutUsRepository.findById(CONTENT_ID)
                .orElseThrow(() -> {
                    log.warn("About Us content not found");
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "About Us not found"
                    );
                });

        return AppInfoContentResponseDto.builder().content(detail.getContent()).createdAt(detail.getCreatedAt()).updatedAt(detail.getUpdatedAt()).version(detail.getVersion()).updatedBy(detail.getUpdatedBy()).build();
    }

    // ---------------------------------------------------------
    // PRIVACY POLICY
    // ---------------------------------------------------------

    @Override
    public AppInfoContentResponseDto getPrivacyPolicy() {

        log.debug("Fetching Privacy Policy content");

        AppInfoPrivacyPolicy detail = privacyPolicyRepository.findById(CONTENT_ID)
                .orElseThrow(() -> {
                    log.warn("Privacy Policy content not found");
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Privacy Policy not found"
                    );
                });
        return AppInfoContentResponseDto.builder().content(detail.getContent()).createdAt(detail.getCreatedAt()).updatedAt(detail.getUpdatedAt()).version(detail.getVersion()).updatedBy(detail.getUpdatedBy()).build();
    }

    // ---------------------------------------------------------
    // TERMS AND CONDITIONS
    // ---------------------------------------------------------

    @Override
    public AppInfoContentResponseDto getTermsAndConditions() {

        log.debug("Fetching Terms and Conditions content");

        AppInfoTermsAndCondition detail = termsAndConditionRepository.findById(CONTENT_ID)
                .orElseThrow(() -> {
                    log.warn("Terms and Conditions content not found");
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Terms and Conditions not found"
                    );
                });
        return AppInfoContentResponseDto.builder().content(detail.getContent()).createdAt(detail.getCreatedAt()).updatedAt(detail.getUpdatedAt()).version(detail.getVersion()).updatedBy(detail.getUpdatedBy()).build();
    }
}
