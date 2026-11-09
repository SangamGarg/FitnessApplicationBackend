package com.fitnessapp.support.service;

import com.fitnessapp.portal.dev.assetUploadDev.model.AppMediaAssetEntity;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoEntity;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;
import com.fitnessapp.support.portalSupport.repo.AppInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {
    private final AppInfoRepository appInfoRepository;


    @Override
    public ResponseEntity<?> getAppInfo() {
        try {
            Optional<AppInfoEntity> appInfo = appInfoRepository.findById(1L);
            if (appInfo.isPresent()) {
                return ResponseEntity.ok(appInfo.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("App information has not been initialized");
            }

        } catch (Exception e) {
            log.error("Request failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("An error occurred while fetching app info");
        }
    }

    @Override
    public AppInfoAboutUs getAboutUs() {
        return appInfoRepository.findById(1L)
                .map(AppInfoEntity::getAppInfoAboutUs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "About Us not found"));
    }

    @Override
    public AppInfoPrivacyPolicy getPrivacyPolicy() {
        return appInfoRepository.findById(1L)
                .map(AppInfoEntity::getAppInfoPrivacyPolicy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Privacy Policy not found"));
    }

    @Override
    public AppInfoTermsAndCondition getTermsAndConditions() {
        return appInfoRepository.findById(1L)
                .map(AppInfoEntity::getAppInfoTermsAndCondition)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terms and Conditions not found"));
    }


    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private AppMediaAssetEntity getImageUrlAndAbout() {
//        try {
//            return imageAndAboutRepository.findByNameIgnoreCase().orElse(AppMediaAssetEntity.builder().imageUrl("Not Found").build());
//        } catch (Exception e) {
//            return new AppMediaAssetEntity(0L, "Error Getting Name", "Error Getting Image", "Error Getting About");
//        }
        return null;

    }

}
