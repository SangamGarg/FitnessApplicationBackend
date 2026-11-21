package com.fitnessapp.profile.service;

import com.fitnessapp.appAuth.model.entity.UserDetailsEntity;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserDetailsRepo;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.assets.service.FileStorageService;
import com.fitnessapp.gamify.gamification.model.entity.UserStreakEntity;
import com.fitnessapp.gamify.gamification.model.entity.UserXpAccountEntity;
import com.fitnessapp.gamify.gamification.repository.UserStreakRepository;
import com.fitnessapp.gamify.gamification.repository.UserXpAccountRepository;
import com.fitnessapp.profile.model.dto.request.BadgeAddRequestDto;
import com.fitnessapp.profile.model.dto.response.BadgeAddResponseDto;
import com.fitnessapp.profile.model.dto.response.BadgeResponseDto;
import com.fitnessapp.profile.model.dto.response.ProfileHomeResponseDto;
import com.fitnessapp.profile.model.dto.response.ProgressGalleryResponseDto;
import com.fitnessapp.profile.model.entity.BadgeEntity;
import com.fitnessapp.profile.model.entity.ProgressGalleryEntity;
import com.fitnessapp.profile.model.entity.UserBadgeEntity;
import com.fitnessapp.profile.repository.BadgeRepository;
import com.fitnessapp.profile.repository.ProgressGalleryRepository;
import com.fitnessapp.profile.repository.UserBadgeRepository;
import com.fitnessapp.wallet.service.AppWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    private static final long MAX_PROGRESS_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepo userRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final UserXpAccountRepository xpRepository;
    private final UserStreakRepository streakRepository;
    private final AppWalletService walletService;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final ProgressGalleryRepository progressGalleryRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public ProfileHomeResponseDto getProfileHome() {
        log.debug("Fetching profile home data");
        String userId = currentUserId();
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        UserDetailsEntity details = userDetailsRepo.findByUserId(userId);

        long totalXp = xpRepository.findByUserId(userId)
                .map(UserXpAccountEntity::getTotalXp)
                .orElse(0L);
        int level = (int) Math.floor(Math.sqrt(totalXp / 100.0)) + 1;
        long currentLevelRequiredXp = (long) (level - 1) * (level - 1) * 100L;
        long nextLevelRequiredXp = (long) level * level * 100L;
        long xpToNextLevel = Math.max(0L, nextLevelRequiredXp - totalXp);
        long levelRange = nextLevelRequiredXp - currentLevelRequiredXp;
        double progressPercentage = levelRange == 0 ? 100.0 :
                Math.min(100.0, Math.max(0.0,
                        ((double) (totalXp - currentLevelRequiredXp) / levelRange) * 100.0));

        UserStreakEntity streak = streakRepository.findByUserId(userId).orElse(null);
        Map<Long, Instant> unlocked = unlockedByBadgeId(userId);
        List<BadgeResponseDto> badges = badgeRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(badge -> BadgeResponseDto.from(badge, unlocked.get(badge.getId())))
                .toList();
        List<ProgressGalleryResponseDto> gallery = progressGalleryRepository
                .findByUserIdOrderByUploadedAtDesc(userId, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "uploadedAt")))
                .map(ProgressGalleryResponseDto::from)
                .getContent();
        log.debug("Profile home data fetched successfully. Level: {}, Badges: {}, Gallery images: {}",
                level, badges.size(), gallery.size());
        return ProfileHomeResponseDto.builder()
                .name(details == null ? null : details.getName())
                .email(details == null ? user.getEmail() : details.getEmail())
                .profileImageUrl(details == null ? null : details.getProfileImageUrl())
                .totalCoins(walletService.getBalance().balanceCoins())
                .currentStreak(streak == null ? 0 : streak.getCurrentStreak())
                .longestStreak(streak == null ? 0 : streak.getLongestStreak())
                .lastActiveDate(streak == null ? null : streak.getLastActiveDate())
                .xp(ProfileHomeResponseDto.XpProgressDto.builder()
                        .currentLevel(level)
                        .currentXp(totalXp)
                        .currentLevelRequiredXp(currentLevelRequiredXp)
                        .nextLevel(level + 1)
                        .nextLevelRequiredXp(nextLevelRequiredXp)
                        .xpToNextLevel(xpToNextLevel)
                        .progressPercentage(Math.round(progressPercentage * 100.0) / 100.0)
                        .build())
                .badges(badges)
                .progressGallery(gallery)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponseDto> getAllBadges() {
        log.debug("Fetching all active badges");

        String userId = currentUserId();
        Map<Long, Instant> unlocked = unlockedByBadgeId(userId);

        List<BadgeResponseDto> badges = badgeRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(badge -> BadgeResponseDto.from(badge, unlocked.get(badge.getId())))
                .toList();

        log.debug("Successfully fetched {} all badges", badges.size());
        return badges;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponseDto> getUnlockedBadges() {
        log.debug("Fetching unlocked badges");

        String userId = currentUserId();

        List<BadgeResponseDto> badges =
                userBadgeRepository.findByUserIdAndUnlockedTrueOrderByUnlockedAtDesc(userId).stream()
                        .filter(ownership -> ownership.getBadge().isActive())
                        .map(ownership -> BadgeResponseDto.from(
                                ownership.getBadge(), ownership.getUnlockedAt()))
                        .toList();

        log.debug("Successfully fetched {} unlocked badges", badges.size());
        return badges;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeResponseDto> getLockedBadges() {
        log.debug("Fetching locked badges");

        String userId = currentUserId();
        Map<Long, Instant> unlocked = unlockedByBadgeId(userId);

        List<BadgeResponseDto> badges = badgeRepository.findByActiveTrueOrderByIdAsc().stream()
                .filter(badge -> !unlocked.containsKey(badge.getId()))
                .map(badge -> BadgeResponseDto.from(badge, null))
                .toList();

        log.debug("Successfully fetched {} locked badges", badges.size());
        return badges;
    }

    @Override
    @Transactional(readOnly = true)
    public BadgeResponseDto getBadgeDetails(Long badgeId) {
        log.debug("Fetching badge details. Badge ID: {}", badgeId);

        String userId = currentUserId();

        BadgeEntity badge = badgeRepository.findByIdAndActiveTrue(badgeId)
                .orElseThrow(() -> {
                    log.warn("Active badge not found. Badge ID: {}", badgeId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Badge not found");
                });

        Instant unlockedAt = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .filter(UserBadgeEntity::isUnlocked)
                .map(UserBadgeEntity::getUnlockedAt)
                .orElse(null);

        log.debug("Successfully fetched badge details. Badge ID: {}", badgeId);
        return BadgeResponseDto.from(badge, unlockedAt);
    }

    @Override
    @Transactional
    public BadgeResponseDto unlockBadge(String userId, Long badgeId) {
        log.info("Processing badge unlock. Badge ID: {}", badgeId);

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Badge unlock failed: user not found");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        BadgeEntity badge = badgeRepository.findByIdAndActiveTrue(badgeId)
                .orElseThrow(() -> {
                    log.warn("Badge unlock failed: badge not found or inactive. Badge ID: {}", badgeId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Badge not found or inactive");
                });

        Optional<UserBadgeEntity> existing =
                userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId);

        if (existing.isPresent()) {
            log.debug("Badge ownership already exists. Badge ID: {}", badgeId);

            return BadgeResponseDto.from(
                    badge,
                    existing.get().isUnlocked() ? existing.get().getUnlockedAt() : null
            );
        }

        UserBadgeEntity ownership =
                userBadgeRepository.saveAndFlush(new UserBadgeEntity(user, badge));

        log.info("Badge successfully unlocked. Badge ID: {}", badgeId);

        return BadgeResponseDto.from(badge, ownership.getUnlockedAt());
    }


    @Override
    @Transactional
    public ProgressGalleryResponseDto uploadProgressImage(
            MultipartFile file,
            String caption
    ) {
        log.info("Starting progress image upload");

        validateProgressImage(file);

        if (caption != null && caption.length() > 500) {
            log.warn("Progress image upload rejected: caption exceeds 500 characters");

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Caption must be 500 characters or fewer");
        }

        String userId = currentUserId();

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Progress image upload failed: user account not found");

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User account not found");
                });

        // Step 1: Upload image to external storage.
        String imageUrl = fileStorageService.uploadFile(
                file,
                "progress-gallery/" + userId
        );

        try {
            // Step 2: Persist gallery record.
            // saveAndFlush() forces the SQL insert before returning.
            ProgressGalleryEntity saved = progressGalleryRepository.saveAndFlush(
                    new ProgressGalleryEntity(
                            user,
                            imageUrl,
                            caption == null ? null : caption.trim()
                    )
            );

            log.info(
                    "Progress image uploaded successfully. Image ID: {}, File size: {} bytes",
                    saved.getId(),
                    file.getSize()
            );

            return ProgressGalleryResponseDto.from(saved);

        } catch (RuntimeException databaseException) {

            log.error(
                    "Database persistence failed after image upload. Attempting external file cleanup"
            );

            // Step 3: Roll back the external storage operation.
            try {
                fileStorageService.deleteFileByUrl(imageUrl);

                log.info("Orphaned progress image cleaned up successfully");

            } catch (RuntimeException cleanupException) {

                // Never hide the original database exception.
                log.error(
                        "Failed to clean up orphaned progress image. Manual cleanup may be required",
                        cleanupException
                );
            }

            // Step 4: Preserve the original exception.
            throw databaseException;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProgressGalleryResponseDto> getProgressGallery(Pageable pageable) {
        log.debug("Fetching progress gallery. Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        String userId = currentUserId();

        Page<ProgressGalleryResponseDto> response = progressGalleryRepository
                .findByUserIdOrderByUploadedAtDesc(userId, pageable)
                .map(ProgressGalleryResponseDto::from);

        log.debug("Progress gallery fetched. Page: {}, Records: {}, Total elements: {}",
                response.getNumber(), response.getNumberOfElements(), response.getTotalElements());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressGalleryResponseDto getProgressImage(Long imageId) {
        log.debug("Fetching progress image. Image ID: {}", imageId);

        String userId = currentUserId();

        ProgressGalleryEntity image = progressGalleryRepository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> {
                    log.warn("Progress image not found. Image ID: {}", imageId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Progress image not found");
                });

        log.debug("Progress image fetched successfully. Image ID: {}", imageId);

        return ProgressGalleryResponseDto.from(image);
    }

    @Override
    @Transactional
    public void deleteProgressImage(Long imageId) {
        log.info("Starting progress image deletion. Image ID: {}", imageId);

        String userId = currentUserId();

        ProgressGalleryEntity image = progressGalleryRepository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> {
                    log.warn("Progress image deletion failed: image not found. Image ID: {}", imageId);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Progress image not found");
                });

        fileStorageService.deleteFileByUrl(image.getImageUrl());
        progressGalleryRepository.delete(image);

        log.info("Progress image deleted successfully. Image ID: {}", imageId);
    }

    @Override
    @Transactional
    public BadgeAddResponseDto addBadges(BadgeAddRequestDto request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge data is required");
        }

        String name = request.getName() == null ? null : request.getName().trim();
        String description = request.getDescription() == null ? null : request.getDescription().trim();

        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge name is required");
        }
        if (name.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge name must be 100 characters or fewer");
        }
        if (description == null || description.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge description is required");
        }
        if (description.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge description must be 1000 characters or fewer");
        }
        if (request.getImageUrl() != null && request.getImageUrl().length() > 2048) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge image URL must be 2048 characters or fewer");
        }
        if (request.getCategory() != null && request.getCategory().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge category must be 50 characters or fewer");
        }
        if (request.getCriteriaMetadata() != null && request.getCriteriaMetadata().length() > 4000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Badge criteria metadata must be 4000 characters or fewer");
        }
        if (badgeRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A badge with this name already exists");
        }

        BadgeEntity badge = new BadgeEntity(
                name,
                description,
                normalizeOptional(request.getImageUrl()),
                normalizeOptional(request.getCategory())
        );
        badge.setCriteriaMetadata(normalizeOptional(request.getCriteriaMetadata()));
        badge.setActive(request.isActive());

        BadgeEntity saved = badgeRepository.saveAndFlush(badge);
        BadgeResponseDto response = BadgeResponseDto.from(saved, null);
        log.info("Badge created successfully. Badge ID: {}, name: {}", saved.getId(), saved.getName());
        return new BadgeAddResponseDto(response, "Badge added successfully");
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Map<Long, Instant> unlockedByBadgeId(String userId) {
        log.debug("Building unlocked badge lookup");

        Map<Long, Instant> result = new HashMap<>();

        userBadgeRepository.findByUserIdAndUnlockedTrueOrderByUnlockedAtDesc(userId)
                .forEach(ownership ->
                        result.put(ownership.getBadge().getId(), ownership.getUnlockedAt()));

        log.debug("Unlocked badge lookup completed. Count: {}", result.size());

        return result;
    }


    private void validateProgressImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Progress image validation failed: empty or missing file");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Image file is required");
        }

        if (file.getSize() > MAX_PROGRESS_IMAGE_SIZE) {
            log.warn("Progress image validation failed: file exceeds maximum size. Size: {} bytes",
                    file.getSize());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Image must be 5 MB or smaller");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {

            log.warn("Progress image validation failed: unsupported content type");

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPEG, PNG and WEBP images are allowed");
        }

        try (InputStream inputStream = file.getInputStream()) {

            // Read only the first 12 bytes required for signature validation.
            byte[] header = inputStream.readNBytes(12);

            boolean jpeg = header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;

            boolean png = header.length >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47
                    && header[4] == 0x0D
                    && header[5] == 0x0A
                    && header[6] == 0x1A
                    && header[7] == 0x0A;

            boolean webp = header.length >= 12
                    && header[0] == 'R'
                    && header[1] == 'I'
                    && header[2] == 'F'
                    && header[3] == 'F'
                    && header[8] == 'W'
                    && header[9] == 'E'
                    && header[10] == 'B'
                    && header[11] == 'P';

            if (!jpeg && !png && !webp) {
                log.warn("Progress image validation failed: invalid image signature");

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid image file content");
            }

            // Ensure the declared MIME type matches the actual file signature.
            boolean contentTypeMatches =
                    ("image/jpeg".equalsIgnoreCase(contentType) && jpeg)
                            || ("image/png".equalsIgnoreCase(contentType) && png)
                            || ("image/webp".equalsIgnoreCase(contentType) && webp);

            if (!contentTypeMatches) {
                log.warn("Progress image validation failed: MIME type does not match file signature");

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Image content does not match the declared file type");
            }

            log.debug("Progress image validation successful");

        } catch (IOException e) {
            log.error("Unable to read uploaded progress image", e);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read image file");
        }
    }


    private String currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null ||
                "anonymousUser".equals(authentication.getName())) {

            log.warn("Unauthenticated attempt to access profile service");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return authentication.getName();
    }
}
