package com.fitnessapp.portal.dev.appInfoDev;

import com.fitnessapp.portal.dev.appInfoDev.models.dto.request.*;
import com.fitnessapp.portal.dev.appInfoDev.models.dto.response.AppInfoFaqsResponseDto;
import com.fitnessapp.portal.dev.appInfoDev.models.dto.response.AppInfoLinksResponseDto;
import com.fitnessapp.portal.dev.appInfoDev.models.dto.response.AppInfoResponseDto;
import com.fitnessapp.portal.dev.appInfoDev.models.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppInfoServiceImpl implements AppInfoService {

    private static final Long APP_INFO_SINGLETON_ID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final AppInfoRepository appInfoRepository;

    @Value("${version.app:1.0.0}")
    private String version;

    private String currentTimestamp() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }

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
                            .createdAt(currentTimestamp())
                            .version(version)
                            .appInfoLinks(new ArrayList<>())
                            .appInfoFaqs(new ArrayList<>())
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
                .lastUpdatedBy(currentTimestamp())
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
                .lastUpdatedBy(currentTimestamp())
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
                .lastUpdatedBy(currentTimestamp())
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
    @Transactional
    public Map<String, String> uploadAppLinksUrl(List<AppInfoLinksRequestDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide at least one app link");
        }

        log.info("Adding {} app link(s)", dtoList.size());
        AppInfoEntity entity = getOrCreateAppInfoForWrite();

        // Compare trimmed, case-insensitive titles and URLs to avoid accidental duplicates.
        var existingTitles = new java.util.HashSet<String>();
        var existingUrls = new java.util.HashSet<String>();
        entity.getAppInfoLinks().forEach(link -> {
            existingTitles.add(normalizeDuplicateKey(link.getTitle()));
            existingUrls.add(normalizeDuplicateKey(link.getUrl()));
        });

        List<AppInfoLinks> linksToAdd = new ArrayList<>();
        for (AppInfoLinksRequestDto dto : dtoList) {
            if (dto == null || dto.getTitle() == null || dto.getUrl() == null
                    || dto.getTitle().isBlank() || dto.getUrl().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each app link must have a title and URL");
            }

            String title = dto.getTitle().trim();
            String url = dto.getUrl().trim();
            String titleKey = normalizeDuplicateKey(title);
            String urlKey = normalizeDuplicateKey(url);

            if (!existingTitles.add(titleKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "An app link with this title already exists: " + title);
            }
            if (!existingUrls.add(urlKey)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "An app link with this URL already exists: " + url);
            }

            linksToAdd.add(AppInfoLinks.builder().title(title).url(url).build());
        }

        // Append only. Existing links are deliberately preserved.
        entity.getAppInfoLinks().addAll(linksToAdd);
        appInfoRepository.save(entity);

        log.info("Added {} app link(s); total is now {}", linksToAdd.size(), entity.getAppInfoLinks().size());
        return Map.of("message", linksToAdd.size() + " app link(s) added successfully");
    }

    private String normalizeDuplicateKey(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    @Transactional
    public Map<String, String> patchAppLinksUrl(AppInfoLinksRequestDto dto) {
        log.info("Patching app link with title: [{}]", dto.getTitle());
        AppInfoEntity entity = getOrCreateAppInfoForWrite();

        if (dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getUrl() == null || dto.getUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link title and URL are required");
        }

        String requestedTitle = dto.getTitle().trim();
        String requestedUrl = dto.getUrl().trim();
        AppInfoLinks target = entity.getAppInfoLinks().stream()
                .filter(link -> link.getTitle() != null && link.getTitle().trim().equalsIgnoreCase(requestedTitle))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Link with title [{}] not found for update", requestedTitle);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Link title not found");
                });

        boolean duplicateUrl = entity.getAppInfoLinks().stream()
                .filter(link -> !Objects.equals(link.getId(), target.getId()))
                .anyMatch(link -> normalizeDuplicateKey(link.getUrl()).equals(normalizeDuplicateKey(requestedUrl)));
        if (duplicateUrl) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another app link already uses this URL");
        }

        target.setUrl(requestedUrl);

        appInfoRepository.save(entity);
        log.info("Link [{}] updated successfully", dto.getTitle());
        return Map.of("message", "Link updated successfully");
    }

    @Override
    @Transactional
    public Map<String, String> uploadAppFqs(List<AppInfoFaqsRequestDto> dtoList) {
        log.info("Uploading {} FAQs", dtoList.size());
        AppInfoEntity entity = getOrCreateAppInfoForWrite();

        List<AppInfoFaqs> newFaqs = dtoList.stream()
                .map(d -> AppInfoFaqs.builder()
                        .question(d.getQuestion().trim())
                        .answer(d.getAnswer().trim())
                        .build())
                .toList();

        entity.getAppInfoFaqs().clear();
        entity.getAppInfoFaqs().addAll(newFaqs);
        appInfoRepository.save(entity);

        log.info("Successfully updated FAQs list");
        return Map.of("message", "FAQs uploaded successfully");
    }

    @Override
    @Transactional
    public Map<String, String> patchAppFaqs(AppInfoFaqsRequestDto dto, Long id) {
        log.info("Patching FAQ with ID: [{}]", id);
        AppInfoEntity entity = getOrCreateAppInfoForWrite();

        AppInfoFaqs targetFaq = entity.getAppInfoFaqs().stream()
                .filter(faq -> Objects.equals(faq.getId(), id))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("FAQ with ID [{}] not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "FAQ with ID " + id + " not found");
                });

        targetFaq.setQuestion(dto.getQuestion().trim());
        targetFaq.setAnswer(dto.getAnswer().trim());

        appInfoRepository.save(entity);
        log.info("FAQ [{}] updated successfully", id);
        return Map.of("message", "FAQ updated successfully");
    }
    @Override
    @Transactional(readOnly = true)
    public AppInfoResponseDto getCompleteAppInfo() {
        log.debug("Fetching complete AppInfo dataset");
        AppInfoEntity entity = getAppInfoForRead();

        List<AppInfoLinksResponseDto> links = entity.getAppInfoLinks().stream()
                .map(l -> new AppInfoLinksResponseDto(l.getId(), l.getTitle(), l.getUrl()))
                .toList();

        List<AppInfoFaqsResponseDto> faqs = entity.getAppInfoFaqs().stream()
                .map(f -> new AppInfoFaqsResponseDto(f.getId(), f.getQuestion(), f.getAnswer()))
                .toList();

        return AppInfoResponseDto.builder()
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .aboutUs(entity.getAppInfoAboutUs())
                .privacyPolicy(entity.getAppInfoPrivacyPolicy())
                .termsAndCondition(entity.getAppInfoTermsAndCondition())
                .links(links)
                .faqs(faqs)
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

    @Override
    @Transactional(readOnly = true)
    public List<AppInfoLinksResponseDto> getAppLinks() {
        log.debug("Fetching all App Links");
        return getAppInfoForRead().getAppInfoLinks().stream()
                .map(link -> new AppInfoLinksResponseDto(link.getId(), link.getTitle(), link.getUrl()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppInfoFaqsResponseDto> getAppFaqs() {
        log.debug("Fetching all FAQs");
        return getAppInfoForRead().getAppInfoFaqs().stream()
                .map(faq -> new AppInfoFaqsResponseDto(faq.getId(), faq.getQuestion(), faq.getAnswer()))
                .toList();
    }
}
