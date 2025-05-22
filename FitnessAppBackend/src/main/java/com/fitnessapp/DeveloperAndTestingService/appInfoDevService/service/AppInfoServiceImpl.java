package com.fitnessapp.DeveloperAndTestingService.appInfoDevService.service;

import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.dto.*;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.models.entity.*;
import com.fitnessapp.DeveloperAndTestingService.appInfoDevService.repository.AppInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppInfoServiceImpl implements AppInfoService {

    @Value("${version.app}")
    private String version;

    LocalDateTime now = LocalDateTime.now();
    String formattedDateAndTime = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

    private final AppInfoRepository appInfoRepository;

    public AppInfoServiceImpl(AppInfoRepository appInfoRepository) {

        this.appInfoRepository = appInfoRepository;
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadAboutUs(AppInfoAboutUsRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .createdAt(formattedDateAndTime)
                    .version(version)
                    .appInfoAboutUs(AppInfoAboutUs.builder()
                            .content(dto.getContent()).lastUpdatedBy(formattedDateAndTime).build())
                    .build());
            return ResponseEntity.ok("About Us Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .appInfoPrivacyPolicy(AppInfoPrivacyPolicy.builder()
                            .content(dto.getContent()).lastUpdatedBy(formattedDateAndTime).build()
                    ).build());
            return ResponseEntity.ok("Privacy Policy  Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadTermsAndCondition(AppInfoTermsAndConditionRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .appInfoTermsAndCondition(AppInfoTermsAndCondition.builder()
                            .content(dto.getContent()).lastUpdatedBy(formattedDateAndTime).build())
                    .build());
            return ResponseEntity.ok("Terms And Conditions Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchAboutUs(AppInfoAboutUsRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .appInfoAboutUs(AppInfoAboutUs.builder()
                            .content(dto.getContent()).lastUpdatedBy(formattedDateAndTime).build())
                    .build());
            return ResponseEntity.ok("About Us Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchPrivacyPolicy(AppInfoPrivacyPolicyRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .appInfoPrivacyPolicy(AppInfoPrivacyPolicy.builder()
                            .content(dto.getContent()).lastUpdatedBy(formattedDateAndTime).build()
                    ).build());
            return ResponseEntity.ok("Privacy Policy  Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchTermsAndCondition(AppInfoTermsAndConditionRequestDto dto) {
        try {
            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .appInfoTermsAndCondition(AppInfoTermsAndCondition.builder()
                            .content(dto.getContent()).lastUpdatedBy(formattedDateAndTime).build())
                    .build());
            return ResponseEntity.ok("Terms And Conditions Updated Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> uploadAppLinksUrl(List<AppInfoLinksRequestDto> appInfoLinksRequestDto) {
        try {
            List<AppInfoLinks> listOfLinks = appInfoLinksRequestDto.stream()
                    .map(dto -> AppInfoLinks.builder()
                            .title(dto.getTitle())
                            .url(dto.getUrl())
                            .build()).toList();

            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .appInfoLinks(listOfLinks)
                    .build());
            return ResponseEntity.ok("Links Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchAppLinksUrl(AppInfoLinksRequestDto dto) {
        try {
            AppInfoEntity entity = appInfoRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("AppInfoEntity not found"));

            List<AppInfoLinks> updatedLinks = entity.getAppInfoLinks().stream()
                    .map(link -> {
                        if (link.getTitle().equalsIgnoreCase(dto.getTitle())) {
                            link.setUrl(dto.getUrl());
                            link.setTitle(dto.getTitle());
                        }
                        return link;
                    })
                    .collect(Collectors.toList());

            entity.setAppInfoLinks(updatedLinks);
            appInfoRepository.save(entity);

            return ResponseEntity.ok("Link updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }


    @Transactional
    @Override
    public ResponseEntity<?> uploadAppFqs(List<AppInfoFaqsRequestDto> appInfoFaqsRequestDtos) {
        try {
            List<AppInfoFaqs> listOfLinks = appInfoFaqsRequestDtos.stream()
                    .map(dto -> AppInfoFaqs.builder()
                            .question(dto.getQuestion())
                            .answer(dto.getAnswer())
                            .build()).toList();

            appInfoRepository.save(AppInfoEntity.builder()
                    .id(1L)
                    .appInfoFaqs(listOfLinks)
                    .build());
            return ResponseEntity.ok("Faqs Uploaded Successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> patchAppFaqs(AppInfoFaqsRequestDto dto, Long id) {
        try {
            AppInfoEntity entity = appInfoRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("AppInfoEntity not found"));

            List<AppInfoFaqs> updatedFaqs = entity.getAppInfoFaqs().stream()
                    .map(faq -> {
                        if (faq.getId().equals(id)) {
                            faq.setQuestion(dto.getQuestion());
                            faq.setAnswer(dto.getAnswer());
                        }
                        return faq;
                    })
                    .collect(Collectors.toList());

            entity.setAppInfoFaqs(updatedFaqs);
            appInfoRepository.save(entity);

            return ResponseEntity.ok("FAQ updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

}
