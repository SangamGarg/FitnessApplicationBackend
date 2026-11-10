
package com.fitnessapp.support.portalSupport.service;

import com.fitnessapp.support.models.dtos.SupportFaqCategoryDto;
import com.fitnessapp.support.models.entity.SupportFaqCategoryEntity;
import com.fitnessapp.support.models.entity.SupportFaqEntity;
import com.fitnessapp.support.portalSupport.models.dto.request.*;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoAboutUs;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoPrivacyPolicy;
import com.fitnessapp.support.portalSupport.models.entity.AppInfoTermsAndCondition;
import com.fitnessapp.support.portalSupport.repo.AppInfoAboutUsRepository;
import com.fitnessapp.support.portalSupport.repo.AppInfoPrivacyPolicyRepository;
import com.fitnessapp.support.portalSupport.repo.AppInfoTermsAndConditionRepository;
import com.fitnessapp.support.repo.SupportFaqCategoryRepository;
import com.fitnessapp.support.repo.SupportFaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppInfoServiceImpl implements AppInfoService {

    private static final Long CONTENT_ID = 1L;

    private final AppInfoAboutUsRepository aboutUsRepository;
    private final AppInfoPrivacyPolicyRepository privacyPolicyRepository;
    private final AppInfoTermsAndConditionRepository termsAndConditionRepository;
    private final SupportFaqCategoryRepository supportFaqCategoryRepository;
    private final SupportFaqRepository supportFaqRepository;

    // ---------------------------------------------------------
    // ABOUT US
    // ---------------------------------------------------------

    @Override
    @Transactional
    public Map<String, String> uploadAboutUs(AppInfoAboutUsRequestDto dto) {

        log.info("Uploading About Us content");

        saveContent(
                aboutUsRepository,
                () -> AppInfoAboutUs.builder()
                        .id(CONTENT_ID)
                        .build(),
                entity -> {
                    entity.setContent(validateContent(dto.getContent()));
                    entity.setUpdatedBy(getCurrentAdmin());
                }
        );

        log.info("About Us content saved successfully");

        return Map.of("message", "About Us saved successfully");
    }

    @Override
    @Transactional
    public Map<String, String> patchAboutUs(AppInfoAboutUsRequestDto dto) {

        return uploadAboutUs(dto);
    }


    @Override
    @Transactional
    public Map<String, String> uploadPrivacyPolicy(
            AppInfoPrivacyPolicyRequestDto dto) {

        log.info("Uploading Privacy Policy content");

        saveContent(
                privacyPolicyRepository,
                () -> AppInfoPrivacyPolicy.builder()
                        .id(CONTENT_ID)
                        .build(),
                entity -> {
                    entity.setContent(validateContent(dto.getContent()));
                    entity.setUpdatedBy(getCurrentAdmin());
                }
        );

        log.info("Privacy Policy content saved successfully");

        return Map.of("message", "Privacy Policy uploaded successfully");
    }

    @Override
    @Transactional
    public Map<String, String> patchPrivacyPolicy(
            AppInfoPrivacyPolicyRequestDto dto) {

        return uploadPrivacyPolicy(dto);
    }

    // ---------------------------------------------------------
    // TERMS AND CONDITIONS
    // ---------------------------------------------------------

    @Override
    @Transactional
    public Map<String, String> uploadTermsAndCondition(
            AppInfoTermsAndConditionRequestDto dto) {

        log.info("Uploading Terms and Conditions content");

        saveContent(
                termsAndConditionRepository,
                () -> AppInfoTermsAndCondition.builder()
                        .id(CONTENT_ID)
                        .build(),
                entity -> {
                    entity.setContent(validateContent(dto.getContent()));
                    entity.setUpdatedBy(getCurrentAdmin());
                }
        );

        log.info("Terms and Conditions content saved successfully");

        return Map.of("message", "Terms and Conditions uploaded successfully");
    }

    @Override
    @Transactional
    public Map<String, String> patchTermsAndCondition(
            AppInfoTermsAndConditionRequestDto dto) {

        return uploadTermsAndCondition(dto);
    }

    // ---------------------------------------------------------
    // FAQS
    // ---------------------------------------------------------

    @Override
    @Transactional
    public SupportFaqCategoryDto createCategory(
            SupportFaqCategoryRequestDto requestDto) {

        String categoryKey = requestDto.getCategoryKey()
                .trim()
                .toUpperCase();

        log.info("Creating support FAQ category: {}", categoryKey);

        if (supportFaqCategoryRepository.existsByCategoryKeyIgnoreCase(categoryKey)) {
            throw new IllegalArgumentException(
                    "FAQ category already exists: " + categoryKey
            );
        }

        SupportFaqCategoryEntity entity = SupportFaqCategoryEntity.builder()
                .categoryKey(categoryKey)
                .name(requestDto.getName().trim())
                .description(requestDto.getDescription())
                .iconKey(requestDto.getIconKey())
                .active(requestDto.getActive() != null
                        ? requestDto.getActive()
                        : true)
                .sortOrder(requestDto.getSortOrder() != null
                        ? requestDto.getSortOrder()
                        : 0)
                .build();

        SupportFaqCategoryEntity savedEntity =
                supportFaqCategoryRepository.save(entity);

        log.info("Support FAQ category created successfully. ID: {}",
                savedEntity.getId());

        return SupportFaqCategoryDto.builder()
                .id(savedEntity.getId())
                .categoryKey(savedEntity.getCategoryKey())
                .name(savedEntity.getName())
                .description(savedEntity.getDescription())
                .iconKey(savedEntity.getIconKey())
                .sortOrder(savedEntity.getSortOrder())
                .build();
    }

    @Override
    @Transactional
    public Map<String, String> uploadAppFqs(List<AppInfoFaqsRequestDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FAQ list cannot be empty");
        }
        Map<String, SupportFaqCategoryEntity> categoryCache = new HashMap<>();
        List<SupportFaqEntity> faqEntities = new ArrayList<>();
        for (AppInfoFaqsRequestDto dto : dtoList) {
            if (dto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FAQ entry cannot be null");
            }
            if (dto.getQuestion() == null || dto.getQuestion().isBlank()
                    || dto.getAnswer() == null || dto.getAnswer().isBlank()
                    || dto.getCategoryKey() == null || dto.getCategoryKey().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question, answer and category key are required");
            }
            String categoryKey = dto.getCategoryKey().trim();
            SupportFaqCategoryEntity category = categoryCache.computeIfAbsent(categoryKey.toLowerCase(), key ->
                    supportFaqCategoryRepository
                            .findByCategoryKeyIgnoreCaseAndActiveTrue(categoryKey)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST, "Invalid or inactive FAQ category: " + categoryKey
                            )));

            SupportFaqEntity faq = getSupportFaqEntity(dto, category);
            // If SupportFaqEntity extends your BaseContentEntity:
            faq.setUpdatedBy(getCurrentAdmin());
            faqEntities.add(faq);
        }
        supportFaqRepository.saveAll(faqEntities);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "FAQs uploaded successfully");
        response.put("uploadedCount", String.valueOf(faqEntities.size()));
        return response;
    }

    private static @NonNull SupportFaqEntity getSupportFaqEntity(AppInfoFaqsRequestDto dto, SupportFaqCategoryEntity category) {
        SupportFaqEntity faq = new SupportFaqEntity();

        faq.setQuestion(dto.getQuestion().trim());
        faq.setAnswer(dto.getAnswer().trim());
        faq.setCategory(category);

        faq.setSortOrder(
                dto.getSortOrder() != null ? dto.getSortOrder() : 0
        );

        faq.setPopular(
                dto.getPopular() != null && dto.getPopular()
        );

        faq.setActive(
                dto.getActive() == null || dto.getActive()
        );
        return faq;
    }

    @Override
    @Transactional
    public Map<String, String> patchAppFaqs(
            AppInfoFaqPatchRequestDto dto,
            Long id) {

        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "FAQ update request cannot be empty"
            );
        }

        SupportFaqEntity faq = supportFaqRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "FAQ not found with ID: " + id
                ));

        boolean updated = false;

        if (dto.getQuestion() != null) {
            if (dto.getQuestion().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Question cannot be blank"
                );
            }

            faq.setQuestion(dto.getQuestion().trim());
            updated = true;
        }

        if (dto.getAnswer() != null) {
            if (dto.getAnswer().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Answer cannot be blank"
                );
            }

            faq.setAnswer(dto.getAnswer().trim());
            updated = true;
        }

        if (dto.getCategoryKey() != null) {
            if (dto.getCategoryKey().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category key cannot be blank"
                );
            }

            String categoryKey = dto.getCategoryKey().trim();

            SupportFaqCategoryEntity category =
                    supportFaqCategoryRepository
                            .findByCategoryKeyIgnoreCaseAndActiveTrue(categoryKey)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Invalid or inactive FAQ category: " + categoryKey
                            ));

            faq.setCategory(category);
            updated = true;
        }

        if (dto.getSortOrder() != null) {
            if (dto.getSortOrder() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sort order cannot be negative"
                );
            }

            faq.setSortOrder(dto.getSortOrder());
            updated = true;
        }

        if (dto.getPopular() != null) {
            faq.setPopular(dto.getPopular());
            updated = true;
        }

        if (dto.getActive() != null) {
            faq.setActive(dto.getActive());
            updated = true;
        }

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided for update"
            );
        }

        faq.setUpdatedBy(getCurrentAdmin());

        supportFaqRepository.save(faq);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "FAQ updated successfully");
        response.put("faqId", String.valueOf(faq.getId()));

        return response;
    }

    // ---------------------------------------------------------
    // COMMON CONTENT SAVE METHOD
    // ---------------------------------------------------------

    private <T> void saveContent(
            JpaRepository<T, Long> repository,
            Supplier<T> entitySupplier,
            Consumer<T> entityUpdater) {

        T entity = repository.findById(CONTENT_ID)
                .orElseGet(entitySupplier);

        entityUpdater.accept(entity);

        repository.save(entity);
    }

    // ---------------------------------------------------------
    // CONTENT VALIDATION
    // ---------------------------------------------------------

    private String validateContent(String content) {

        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content cannot be empty"
            );
        }

        return content.trim();
    }

    // ---------------------------------------------------------
    // CURRENT AUTHENTICATED ADMIN
    // ---------------------------------------------------------

    private String getCurrentAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return authentication.getName();
    }
}
