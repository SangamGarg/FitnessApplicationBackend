package com.fitnessapp.followUsLinks.controller;

import com.fitnessapp.followUsLinks.model.FollowUsLinksPatchRequestDto;
import com.fitnessapp.followUsLinks.model.FollowUsLinksRequestDto;
import com.fitnessapp.followUsLinks.service.FollowUsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_FOLLOW_US_ENDPOINT;

@Slf4j
@RestController
@Validated
@RequestMapping(PORTAL_FOLLOW_US_ENDPOINT)
@RequiredArgsConstructor
public class PortalFollowUsController {

    private final FollowUsService followUsService;

    @PostMapping("/upload/links")
    public ResponseEntity<Map<String, String>> uploadAppLinksUrl(
            @RequestBody
            @NotEmpty(message = "Links list cannot be empty")
            List<@NotNull @Valid FollowUsLinksRequestDto> requestDto) {

        log.info("REST request to upload {} Follow Us links",
                requestDto.size());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(followUsService.uploadAppLinksUrl(requestDto));
    }

    @PatchMapping("/links/{id}")
    public ResponseEntity<Map<String, String>> patchAppLinksUrl(
            @PathVariable Long id,
            @Valid @RequestBody FollowUsLinksPatchRequestDto requestDto) {

        log.info("REST request to update Follow Us link ID [{}]", id);

        return ResponseEntity.ok(
                followUsService.patchAppLinksUrl(id, requestDto)
        );
    }
}