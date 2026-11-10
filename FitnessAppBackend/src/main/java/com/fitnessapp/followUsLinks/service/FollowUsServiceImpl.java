package com.fitnessapp.followUsLinks.service;


import com.fitnessapp.followUsLinks.FollowUsUrlUtils;
import com.fitnessapp.followUsLinks.model.FollowUsLinksEntity;
import com.fitnessapp.followUsLinks.model.FollowUsLinksPatchRequestDto;
import com.fitnessapp.followUsLinks.model.FollowUsLinksRequestDto;
import com.fitnessapp.followUsLinks.model.FollowUsLinksResponseDto;
import com.fitnessapp.followUsLinks.repo.FollowUsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowUsServiceImpl implements FollowUsService {

    private static final int MAX_LINKS_PER_REQUEST = 50;

    private final FollowUsRepo followUsRepo;

    @Override
    public List<FollowUsLinksResponseDto> getAppLinks() {

        log.debug("Fetching Follow Us links");

        return followUsRepo.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public Map<String, String> uploadAppLinksUrl(
            List<FollowUsLinksRequestDto> dtoList) {

        validateUploadList(dtoList);

        log.info("Uploading {} Follow Us links", dtoList.size());

        Set<String> existingTitleKeys = new HashSet<>();
        Set<String> existingUrlKeys = new HashSet<>();

        // Load existing links once, rather than querying once per DTO.
        followUsRepo.findAll().forEach(link -> {
            existingTitleKeys.add(link.getTitleKey());
            existingUrlKeys.add(link.getNormalizedUrl());
        });

        List<FollowUsLinksEntity> linksToSave =
                new ArrayList<>(dtoList.size());

        for (FollowUsLinksRequestDto dto : dtoList) {

            if (dto == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Link entry cannot be null"
                );
            }

            String title = validateTitle(dto.getTitle());
            String subTitle = validateSubTitle(dto.getSubtitle());
            String url = validateAndTrimUrl(dto.getUrl());

            String titleKey = FollowUsUrlUtils.normalizeTitle(title);
            String normalizedUrl = FollowUsUrlUtils.normalizeUrl(url);

            if (!existingTitleKeys.add(titleKey)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A link with this title already exists: " + title
                );
            }

            if (!existingUrlKeys.add(normalizedUrl)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A link with this URL already exists"
                );
            }

            FollowUsLinksEntity entity = FollowUsLinksEntity.builder()
                    .title(title)
                    .subTitle(subTitle)
                    .url(url)
                    .titleKey(titleKey)
                    .iconKey(dto.getIconKey().name())
                    .normalizedUrl(normalizedUrl)
                    .version(0L)
                    .updatedBy(getCurrentAdmin())
                    .build();

            linksToSave.add(entity);
        }

        try {
            followUsRepo.saveAllAndFlush(linksToSave);
        } catch (DataIntegrityViolationException exception) {
            log.warn("Database uniqueness constraint rejected Follow Us upload");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "One or more Follow Us links already exist"
            );
        }

        log.info("Successfully uploaded {} Follow Us links",
                linksToSave.size());

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Follow Us links uploaded successfully");
        response.put("uploadedCount", String.valueOf(linksToSave.size()));

        return response;
    }

    @Override
    @Transactional
    public Map<String, String> patchAppLinksUrl(
            Long id,
            FollowUsLinksPatchRequestDto dto) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid link ID"
            );
        }

        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Update request cannot be empty"
            );
        }

        FollowUsLinksEntity entity = followUsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Follow Us link not found with ID: " + id
                ));

        boolean updated = false;

        if (dto.getTitle() != null) {

            String title = validateTitle(dto.getTitle());
            String subTitle = validateSubTitle(dto.getSubtitle());
            String titleKey = FollowUsUrlUtils.normalizeTitle(title);

            if (!titleKey.equals(entity.getTitleKey())
                    && followUsRepo.existsByTitleKeyAndIdNot(titleKey, id)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Another link already uses this title"
                );
            }

            entity.setTitle(title);
            entity.setSubTitle(subTitle);
            entity.setTitleKey(titleKey);
            updated = true;
        }

        if (dto.getUrl() != null) {

            String url = validateAndTrimUrl(dto.getUrl());
            String normalizedUrl = FollowUsUrlUtils.normalizeUrl(url);

            if (!normalizedUrl.equals(entity.getNormalizedUrl())
                    && followUsRepo.existsByNormalizedUrlAndIdNot(
                    normalizedUrl, id)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Another link already uses this URL"
                );
            }

            entity.setUrl(url);
            entity.setNormalizedUrl(normalizedUrl);
            entity.setUpdatedBy(getCurrentAdmin());
            updated = true;
        }

        if (!updated) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide at least one field to update"
            );
        }

        try {
            followUsRepo.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            log.warn("Database uniqueness constraint rejected update for link ID [{}]", id);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another link already uses the supplied title or URL"
            );
        }

        log.info("Updated Follow Us link ID [{}]", id);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Follow Us link updated successfully");
        response.put("id", String.valueOf(id));

        return response;
    }

    private void validateUploadList(List<FollowUsLinksRequestDto> dtoList) {

        if (dtoList == null || dtoList.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide at least one app link"
            );
        }

        if (dtoList.size() > MAX_LINKS_PER_REQUEST) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You can upload a maximum of "
                            + MAX_LINKS_PER_REQUEST + " links at once"
            );
        }
    }

    private String validateTitle(String title) {

        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title cannot be blank"
            );
        }

        String trimmed = title.trim();

        if (trimmed.length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title cannot exceed 120 characters"
            );
        }

        return trimmed;
    }


    private String validateSubTitle(String subTitle) {

        if (subTitle == null || subTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sub Title cannot be blank"
            );
        }

        String trimmed = subTitle.trim();

        if (trimmed.length() > 60) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sub Title cannot exceed 60 characters"
            );
        }

        return trimmed;
    }

    private String validateAndTrimUrl(String url) {

        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL cannot be blank"
            );
        }

        String trimmed = url.trim();

        if (trimmed.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL cannot exceed 500 characters"
            );
        }

        // Also validates the HTTPS URI structure.
        FollowUsUrlUtils.normalizeUrl(trimmed);

        return trimmed;
    }

    private FollowUsLinksResponseDto toResponseDto(
            FollowUsLinksEntity entity) {

        return FollowUsLinksResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .subtitle(entity.getSubTitle())
                .iconKey(entity.getIconKey())
                .url(entity.getUrl())
                .build();
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