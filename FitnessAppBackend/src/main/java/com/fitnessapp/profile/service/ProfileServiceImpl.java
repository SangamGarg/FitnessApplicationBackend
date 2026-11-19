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
    public ProgressGalleryResponseDto uploadProgressImage(MultipartFile file, String caption) {
        log.info("Starting progress image upload");

        validateProgressImage(file);

        if (caption != null && caption.length() > 500) {
            log.warn("Progress image upload rejected: caption exceeds 500 characters");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Caption must be 500 characters or fewer");
        }

        String userId = currentUserId();

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Progress image upload failed: user account not found");
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User account not found");
                });

        String imageUrl = fileStorageService.uploadFile(file, "progress-gallery/" + userId);

        ProgressGalleryEntity saved = progressGalleryRepository.save(
                new ProgressGalleryEntity(user, imageUrl, caption == null ? null : caption.trim())
        );

        log.info("Progress image uploaded successfully. Image ID: {}, File size: {} bytes",
                saved.getId(), file.getSize());

        return ProgressGalleryResponseDto.from(saved);
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

        if (file.getContentType() == null ||
                !ALLOWED_IMAGE_TYPES.contains(file.getContentType().toLowerCase(Locale.ROOT))) {
            log.warn("Progress image validation failed: unsupported content type");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only JPEG, PNG and WEBP images are allowed");
        }

        byte[] bytes;

        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error("Unable to read uploaded progress image", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unable to read image file");
        }

        boolean jpeg = bytes.length >= 3 &&
                (bytes[0] & 0xff) == 0xff &&
                (bytes[1] & 0xff) == 0xd8 &&
                (bytes[2] & 0xff) == 0xff;

        boolean png = bytes.length >= 8 &&
                (bytes[0] & 0xff) == 0x89 &&
                bytes[1] == 0x50 &&
                bytes[2] == 0x4e &&
                bytes[3] == 0x47;

        boolean webp = bytes.length >= 12 &&
                bytes[0] == 'R' &&
                bytes[1] == 'I' &&
                bytes[2] == 'F' &&
                bytes[3] == 'F' &&
                bytes[8] == 'W' &&
                bytes[9] == 'E' &&
                bytes[10] == 'B' &&
                bytes[11] == 'P';

        if (!jpeg && !png && !webp) {
            log.warn("Progress image validation failed: file signature does not match an allowed image type");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid image file content");
        }

        log.debug("Progress image validation successful");
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
