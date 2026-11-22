package com.fitnessapp.profile.controller;

import com.fitnessapp.profile.model.dto.request.BadgeAddRequestDto;
import com.fitnessapp.profile.model.dto.request.BadgeStatusRequestDto;
import com.fitnessapp.profile.model.dto.request.BadgeUpdateRequestDto;
import com.fitnessapp.profile.model.dto.response.BadgeResponseDto;
import com.fitnessapp.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_APP_PROFILE_ENDPOINT;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(PORTAL_DEV_APP_PROFILE_ENDPOINT)
@Validated
public class PortalProfileController {
    private final ProfileService profileService;

    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponseDto>> getAllBadgesForPortal() {
        log.info("Received request to fetch all badges for portal");

        try {
            List<BadgeResponseDto> response = profileService.getAllBadgesForPortal();
            log.info("Successfully fetched all badges. Count: {}", response.size());

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch all badges", ex);
            throw ex;
        }
    }

    @GetMapping("/badges/{badgeId}")
    public ResponseEntity<BadgeResponseDto> getBadgeDetailsForPortal(
            @PathVariable Long badgeId
    ) {
        log.info("Received request to fetch badge details for portal. Badge ID: {}", badgeId);

        try {
            BadgeResponseDto response = profileService.getBadgeDetailsForPortal(badgeId);
            log.info("Successfully fetched badge details. Badge ID: {}", badgeId);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch badge details. Badge ID: {}", badgeId, ex);
            throw ex;
        }
    }

    @PostMapping("/add-badges")
    public ResponseEntity<BadgeResponseDto> addBadges(@Valid @RequestBody BadgeAddRequestDto request) {
        log.info("Received request to add badges");
        BadgeResponseDto badgeAddResponseDto = profileService.addBadges(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeAddResponseDto);
    }

    @PatchMapping("/badges/{badgeId}")
    public ResponseEntity<BadgeResponseDto> updateBadge(
            @PathVariable Long badgeId,
            @Valid @RequestBody BadgeUpdateRequestDto request) {
        log.info("Received request to update badge metadata. Badge ID: {}", badgeId);
        return ResponseEntity.ok(profileService.updateBadge(badgeId, request));
    }

    @PatchMapping("/badges/{badgeId}/status")
    public ResponseEntity<BadgeResponseDto> updateBadgeStatus(
            @PathVariable Long badgeId,
            @Valid @RequestBody BadgeStatusRequestDto request) {
        log.info("Received request to update badge status. Badge ID: {}", badgeId);
        return ResponseEntity.ok(profileService.updateBadgeStatus(badgeId, request));
    }

    @DeleteMapping("/badges/{badgeId}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long badgeId) {
        log.info("Received request to delete badge. Badge ID: {}", badgeId);
        profileService.deleteBadge(badgeId);
        return ResponseEntity.noContent().build();
    }
}