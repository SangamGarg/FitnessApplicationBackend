package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.service;

import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.AppInfoAboutUsRequestDto;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.AppInfoPrivacyPolicyRequestDto;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.AppInfoTermsAndConditionRequestDto;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity.AppInfoAboutUs;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity.AppInfoEntity;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity.AppInfoTermsAndCondition;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.repository.AppInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AppInfoServiceImpl implements AppInfoService {

    //set the id as 1 L so the single ifd is created,
    // and later I can also add faqs and url to the entity so it can be expanded


    private final AppInfoRepository appInfoRepository;

    public AppInfoServiceImpl(AppInfoRepository appInfoRepository) {

        this.appInfoRepository = appInfoRepository;
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadAboutUs(AppInfoAboutUsRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder().appInfoAboutUs(AppInfoAboutUs.builder().content(dto.getContent()).build()).build());
            return ResponseEntity.ok("About Us Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder().appInfoPrivacyPolicy(AppInfoPrivacyPolicy.builder().content(dto.getContent()).build()).build());
            return ResponseEntity.ok("Privacy Policy  Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadTermsAndCondition(AppInfoTermsAndConditionRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder().appInfoTermsAndCondition(AppInfoTermsAndCondition.builder().content(dto.getContent()).build()).build());
            return ResponseEntity.ok("Terms And Conditions Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchAboutUs(AppInfoAboutUsRequestDto appInfoAboutUsRequestDto) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchPrivacyPolicy(AppInfoPrivacyPolicyRequestDto appInfoPrivacyPolicyRequestDto) {
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchTermsAndCondition(AppInfoTermsAndConditionRequestDto appInfoTermsAndConditionRequestDto) {
        return null;
    }
}
