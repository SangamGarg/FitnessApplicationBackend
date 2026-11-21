package com.fitnessapp.profile.controller;

import com.fitnessapp.profile.model.dto.request.BadgeAddRequestDto;
import com.fitnessapp.profile.model.dto.response.BadgeAddResponseDto;
import com.fitnessapp.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_DEV_APP_PROFILE_ENDPOINT;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(PORTAL_DEV_APP_PROFILE_ENDPOINT)
@Validated
public class PortalProfileController {
    private final ProfileService profileService;


    @PostMapping("/add-badges")
    public ResponseEntity<BadgeAddResponseDto> addBadges(@Valid @RequestBody BadgeAddRequestDto request) {
        log.info("Received request to add badges");
        BadgeAddResponseDto badgeAddResponseDto = profileService.addBadges(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeAddResponseDto);
    }
}