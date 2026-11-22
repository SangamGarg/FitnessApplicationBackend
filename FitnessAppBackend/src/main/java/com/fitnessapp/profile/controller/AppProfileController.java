
package com.fitnessapp.profile.controller;

import com.fitnessapp.profile.model.dto.response.BadgeResponseDto;
import com.fitnessapp.profile.model.dto.response.ProfileHomeResponseDto;
import com.fitnessapp.profile.model.dto.response.ProgressGalleryResponseDto;
import com.fitnessapp.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_PROFILE_ENDPOINT;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(APP_PROFILE_ENDPOINT)
public class AppProfileController {

    private final ProfileService profileService;

    @GetMapping("/home")
    public ResponseEntity<ProfileHomeResponseDto> getProfileHome() {
        log.info("Received request to fetch profile home");

        try {
            ProfileHomeResponseDto response = profileService.getProfileHome();
            log.info("Successfully fetched profile home");

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch profile home", ex);
            throw ex;
        }
    }

    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponseDto>> getAllBadges() {
        log.info("Received request to fetch all badges");

        try {
            List<BadgeResponseDto> response = profileService.getAllBadges();
            log.info("Successfully fetched all badges. Count: {}", response.size());

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch all badges", ex);
            throw ex;
        }
    }

    @GetMapping("/badges/unlocked")
    public ResponseEntity<List<BadgeResponseDto>> getUnlockedBadges() {
        log.info("Received request to fetch unlocked badges");

        try {
            List<BadgeResponseDto> response = profileService.getUnlockedBadges();
            log.info("Successfully fetched unlocked badges. Count: {}", response.size());

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch unlocked badges", ex);
            throw ex;
        }
    }

    @GetMapping("/badges/locked")
    public ResponseEntity<List<BadgeResponseDto>> getLockedBadges() {
        log.info("Received request to fetch locked badges");

        try {
            List<BadgeResponseDto> response = profileService.getLockedBadges();
            log.info("Successfully fetched locked badges. Count: {}", response.size());

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch locked badges", ex);
            throw ex;
        }
    }

    @GetMapping("/badges/{badgeId}")
    public ResponseEntity<BadgeResponseDto> getBadgeDetails(
            @PathVariable Long badgeId
    ) {
        log.info("Received request to fetch badge details. Badge ID: {}", badgeId);

        try {
            BadgeResponseDto response = profileService.getBadgeDetails(badgeId);
            log.info("Successfully fetched badge details. Badge ID: {}", badgeId);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch badge details. Badge ID: {}", badgeId, ex);
            throw ex;
        }
    }

    @PostMapping("/gallery")
    public ResponseEntity<ProgressGalleryResponseDto> uploadProgressImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String caption
    ) {
        log.info(
                "Received progress image upload request. File size: {} bytes",
                file != null ? file.getSize() : 0
        );

        try {
            ProgressGalleryResponseDto response =
                    profileService.uploadProgressImage(file, caption);

            log.info(
                    "Successfully uploaded progress image. Image ID: {}",
                    response.id()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Failed to upload progress image", ex);
            throw ex;
        }
    }

    @GetMapping("/gallery")
    public ResponseEntity<Page<ProgressGalleryResponseDto>> getProgressGallery(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info(
                "Received progress gallery request. Page: {}, Size: {}",
                page,
                size
        );

        validatePage(page, size);

        try {
            Page<ProgressGalleryResponseDto> response =
                    profileService.getProgressGallery(
                            PageRequest.of(
                                    page,
                                    size,
                                    Sort.by(Sort.Direction.DESC, "uploadedAt")
                            )
                    );

            log.info(
                    "Successfully fetched progress gallery. Page: {}, Records: {}, Total: {}",
                    page,
                    response.getNumberOfElements(),
                    response.getTotalElements()
            );

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error(
                    "Failed to fetch progress gallery. Page: {}, Size: {}",
                    page,
                    size,
                    ex
            );
            throw ex;
        }
    }

    @GetMapping("/gallery/{imageId}")
    public ResponseEntity<ProgressGalleryResponseDto> getProgressImage(
            @PathVariable Long imageId
    ) {
        log.info("Received request to fetch progress image. Image ID: {}", imageId);

        try {
            ProgressGalleryResponseDto response =
                    profileService.getProgressImage(imageId);

            log.info("Successfully fetched progress image. Image ID: {}", imageId);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to fetch progress image. Image ID: {}", imageId, ex);
            throw ex;
        }
    }

    @DeleteMapping("/gallery/{imageId}")
    public ResponseEntity<Void> deleteProgressImage(
            @PathVariable Long imageId
    ) {
        log.info("Received request to delete progress image. Image ID: {}", imageId);

        try {
            profileService.deleteProgressImage(imageId);

            log.info("Successfully deleted progress image. Image ID: {}", imageId);

            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.error("Failed to delete progress image. Image ID: {}", imageId, ex);
            throw ex;
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            log.warn(
                    "Invalid gallery pagination parameters. Page: {}, Size: {}",
                    page,
                    size
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
    }
}
