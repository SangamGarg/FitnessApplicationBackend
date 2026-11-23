package com.fitnessapp.gamify.gamification.service;

import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.assets.service.FileStorageService;
import com.fitnessapp.gamify.achievement.service.AchievementService;
import com.fitnessapp.gamify.challenge.model.enums.ChallengeMetric;
import com.fitnessapp.gamify.challenge.service.ChallengeService;
import com.fitnessapp.gamify.gamification.model.dto.request.CreateGameRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.GameCompleteRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.GameStartRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.PatchGameRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.response.*;
import com.fitnessapp.gamify.gamification.model.entity.*;
import com.fitnessapp.gamify.gamification.model.enums.DurationUnit;
import com.fitnessapp.gamify.gamification.model.enums.GameSessionStatus;
import com.fitnessapp.gamify.gamification.model.enums.GameType;
import com.fitnessapp.gamify.gamification.repository.*;
import com.fitnessapp.wallet.service.AppWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationServiceImpl implements GamificationService {
    private final UserRepo userRepo;
    private final UserXpAccountRepository xpRepo;
    private final RewardLedgerRepository rewardLedgerRepo;
    private final UserStreakRepository streakRepo;
    private final AppWalletService appWalletService;
    private final GameCatalogueRepository catalogueRepo;
    private final GameSessionRepository sessions;
    private final GameDetailRepository gameDetailRepo;
    private final GamificationRewardService rewards;
    private final ChallengeService challenges;
    private final AchievementService achievements;
    private final FileStorageService fileStorageService;


    @Transactional(readOnly = true)
    @Override
    public GamificationHomeSummaryResponseDto getGamificationHomeSummary() {
        String userId = currentUserId();

        userRepo.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
        long xp = xpRepo.findByUserId(userId).map(UserXpAccountEntity::getTotalXp).orElse(0L);
        int level = (int) Math.floor(Math.sqrt(xp / 100.0)) + 1;
        long next = (long) level * level * 100;
        long coins = appWalletService.getBalance().balanceCoins();
        List<GameCatalogueResponseDto> games = getActiveGames();
        UserStreakEntity streak = streakRepo.findByUserId(userId).orElse(null);
        return new GamificationHomeSummaryResponseDto(
                "AI Fitness Games",
                "Play & Challenge",
                "Move, play, and challenge yourself with our AI-powered fitness games and earn rewards",
                games.size(), games, xp, level, coins,
                streak == null ? 0 : streak.getCurrentStreak(), streak == null ? 0 : streak.getLongestStreak(),
                streak == null ? null : streak.getLastActiveDate(), Math.max(0, next - xp));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RewardHistoryResponseDto> getHistory(Pageable pageable) {
        String userId = currentUserId();

        if (!userRepo.existsById(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found");
        return rewardLedgerRepo.findAllByUserIdOrderByCreatedAtDesc(userId, pageable).map(r ->
                new RewardHistoryResponseDto(r.getSourceType(), r.getSourceId(), r.getXpAwarded(), r.getCoinsAwarded(), r.getRewardDate(), r.getCreatedAt()));
    }

    @Transactional
    @Override
    public GameSessionResponseDto start(GameStartRequestDto request) {
        String userId = currentUserId();

        var user = userRepo.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        catalogueRepo.findByGameTypeAndActiveTrue(request.gameType())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game is not available"));

        var old = sessions.findByUserIdAndClientRequestId(userId, request.clientRequestId());

        if (old.isPresent()) {
            if (old.get().getGameType() != request.gameType())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Request ID reused for another game");
            return GameSessionResponseDto.from(old.get());
        }

        return GameSessionResponseDto.from(sessions.save(new GameSessionEntity(user, request.gameType(), request.clientRequestId())));
    }

    @Transactional
    @Override
    public GameSessionResponseDto complete(UUID sessionId, GameCompleteRequestDto request) {
        String userId = currentUserId();

        userRepo.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var s = sessions.findByIdAndUserId(sessionId, userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found"));
        if (s.getStatus() == GameSessionStatus.COMPLETED) return GameSessionResponseDto.from(s);
        if (s.getStatus() != GameSessionStatus.IN_PROGRESS)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game session is not active");

        GameCatalogueEntity catalogue = catalogueRepo.findByGameTypeAndActiveTrue(s.getGameType())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game catalogue entry not found"));

        int gameMinDuration = catalogue.getDurationUnit() == DurationUnit.MINUTE
                ? catalogue.getDurationValue() * 60
                : catalogue.getDurationValue();

        long elapsed = Duration.between(s.getStartedAt(), Instant.now()).getSeconds();
        if (elapsed < gameMinDuration)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game session is too short");
        if (elapsed > 7200) throw new ResponseStatusException(HttpStatus.CONFLICT, "Game session expired");
        if (request.durationSeconds() > elapsed + 15 || request.durationSeconds() < Math.max(1, elapsed - 30))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submitted duration does not match server session duration");

        boolean plank = s.getGameType() == GameType.PLANK_CHALLENGE;
        int reps = plank ? 0 : request.repsDetected();
        Integer hold = plank ? request.holdSeconds() : null;

        if (plank && (hold == null || hold < gameMinDuration || hold > elapsed + 15))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plank hold duration is invalid");
        if (!plank && reps < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one valid repetition is required");
        if (!plank && reps > Math.ceil((elapsed / 60.0) * catalogue.getMaxRepsPerMinute()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repetition count exceeds the allowed rate");

        long score = plank ? Math.multiplyExact((long) hold, 2L) : Math.multiplyExact(reps, 10L);
        s.complete(reps, hold, (int) elapsed, score, request.trackerVersion(), request.modelConfidence());
        sessions.saveAndFlush(s);

        long xp = catalogue.getRewardXp();
        long rewardCoins = catalogue.getRewardCoins();

        rewards.awardExternalReward(userId, "GAME_SESSION:" + s.getId(), "GAME", s.getId().toString(), xp, rewardCoins, "Fitness game reward");
        challenges.recordProgress(userId, ChallengeMetric.GAME_REPS, reps, s.getGameType().name(), s.getId().toString());
        challenges.recordProgress(userId, ChallengeMetric.GAME_SCORE, score, s.getGameType().name(), s.getId().toString());
        if (plank)
            challenges.recordProgress(userId, ChallengeMetric.GAME_HOLD_SECONDS, hold, s.getGameType().name(), s.getId().toString());
        achievements.evaluate(userId);
        return GameSessionResponseDto.from(s);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GameSessionResponseDto> gameHistory(Pageable p) {
        String userId = currentUserId();

        return sessions.findAllByUserIdOrderByStartedAtDesc(userId, p).map(GameSessionResponseDto::from);
    }

    @Transactional(readOnly = true)
    @Override
    public List<GameCatalogueResponseDto> getActiveGames() {
        return catalogueRepo.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(GameCatalogueResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public GameDetailResponseDto gameDetails(GameType type) {
        String userId = currentUserId();
        GameDetailEntity d = gameDetailRepo.findByCatalogueGameTypeAndCatalogueActiveTrue(type)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        return GameDetailResponseDto.from(d, sessions.statsForUser(userId, type));
    }

    @Transactional
    @Override
    public GameCatalogueResponseDto createGame(CreateGameRequestDto request, MultipartFile image,
                                               MultipartFile gif, MultipartFile video) {
        if (catalogueRepo.findByGameType(request.gameType()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already exists");
        }
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game image is required");
        }
        if (gif == null || gif.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game GIF is required");
        }
        if (video == null || video.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game video is required");
        }

        List<String> uploadedUrls = new ArrayList<>();
        try {
            String imageUrl = fileStorageService.uploadMedia(image, "gamification");
            uploadedUrls.add(imageUrl);
            String gifUrl = gif.isEmpty()
                    ? null : fileStorageService.uploadMedia(gif, "gamification");
            if (gifUrl != null) uploadedUrls.add(gifUrl);
            String videoUrl = video.isEmpty()
                    ? null : fileStorageService.uploadMedia(video, "gamification");
            if (videoUrl != null) uploadedUrls.add(videoUrl);

            GameCatalogueEntity catalogue = GameCatalogueEntity.builder()
                    .gameType(request.gameType())
                    .title(request.title().trim())
                    .subTitle(request.subTitle() == null ? null : request.subTitle().trim())
                    .description(request.description().trim())
                    .imageUrl(imageUrl)
                    .level(request.level())
                    .totalRepsRequired(request.totalRepsRequired())
                    .durationValue(request.durationValue())
                    .durationUnit(request.durationUnit())
                    .paid(request.paid())
                    .entryFeeCoins(request.entryFeeCoins())
                    .rewardCoins(request.rewardCoins())
                    .rewardXp(request.rewardXp())
                    .maxRepsPerMinute(request.maxRepsPerMinute())
                    .releasedAt(request.releasedAt())
                    .displayOrder(request.displayOrder())
                    .build();

            catalogue = catalogueRepo.save(catalogue);
            GameDetailEntity details = new GameDetailEntity(
                    catalogue, gifUrl, videoUrl,
                    request.rules() == null ? List.of() : request.rules(),
                    request.tips() == null ? List.of() : request.tips());
            gameDetailRepo.save(details);
            return GameCatalogueResponseDto.from(catalogue);
        } catch (RuntimeException exception) {
            uploadedUrls.forEach(url -> {
                try {
                    fileStorageService.deleteFileByUrl(url);
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            });
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<PortalGameResponseDto> getGamesForPortal() {
        return catalogueRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(catalogue -> PortalGameResponseDto.from(
                        catalogue,
                        gameDetailRepo.findByCatalogue(catalogue).orElse(null)))
                .toList();
    }

    @Transactional
    @Override
    public PortalGameResponseDto patchGame(GameType gameType, PatchGameRequestDto request,
                                           MultipartFile image, MultipartFile gif, MultipartFile video) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game patch data is required");
        }
        GameCatalogueEntity catalogue = catalogueRepo.findByGameType(gameType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        GameDetailEntity detail = gameDetailRepo.findByCatalogue(catalogue).orElse(null);

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game title cannot be blank");
            catalogue.setTitle(title);
        }
        if (request.subTitle() != null) catalogue.setSubTitle(request.subTitle().trim());
        if (request.description() != null) {
            String description = request.description().trim();
            if (description.isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game description cannot be blank");
            catalogue.setDescription(description);
        }
        if (request.level() != null) catalogue.setLevel(request.level());
        if (request.totalRepsRequired() != null) catalogue.setTotalRepsRequired(request.totalRepsRequired());
        if (request.durationValue() != null) catalogue.setDurationValue(request.durationValue());
        if (request.durationUnit() != null) catalogue.setDurationUnit(request.durationUnit());
        if (request.paid() != null) catalogue.setPaid(request.paid());
        if (request.entryFeeCoins() != null) catalogue.setEntryFeeCoins(request.entryFeeCoins());
        if (request.rewardCoins() != null) catalogue.setRewardCoins(request.rewardCoins());
        if (request.rewardXp() != null) catalogue.setRewardXp(request.rewardXp());
        if (request.maxRepsPerMinute() != null) catalogue.setMaxRepsPerMinute(request.maxRepsPerMinute());
        if (request.releasedAt() != null) catalogue.setReleasedAt(request.releasedAt());
        if (request.displayOrder() != null) catalogue.setDisplayOrder(request.displayOrder());
        if (request.active() != null) catalogue.setActive(request.active());

        if (detail == null && (image != null || gif != null || video != null
                || request.rules() != null || request.tips() != null)) {
            detail = new GameDetailEntity(catalogue, null, null,
                    request.rules() == null ? List.of() : request.rules(),
                    request.tips() == null ? List.of() : request.tips());
        }

        try {
            if (image != null && !image.isEmpty()) {
                String oldUrl = catalogue.getImageUrl();
                catalogue.setImageUrl(fileStorageService.uploadMedia(image, "gamification"));
                if (oldUrl != null) fileStorageService.deleteFileByUrl(oldUrl);
            }
            if (detail != null) {
                if (gif != null && !gif.isEmpty()) {
                    String oldUrl = detail.getGifUrl();
                    detail.setGifUrl(fileStorageService.uploadMedia(gif, "gamification"));
                    if (oldUrl != null) fileStorageService.deleteFileByUrl(oldUrl);
                }
                if (video != null && !video.isEmpty()) {
                    String oldUrl = detail.getVideoUrl();
                    detail.setVideoUrl(fileStorageService.uploadMedia(video, "gamification"));
                    if (oldUrl != null) fileStorageService.deleteFileByUrl(oldUrl);
                }
                if (request.rules() != null) detail.setRules(request.rules());
                if (request.tips() != null) detail.setTips(request.tips());
                gameDetailRepo.save(detail);
            }
        } catch (RuntimeException exception) {
            throw exception;
        }

        catalogueRepo.save(catalogue);
        return PortalGameResponseDto.from(catalogue, detail);
    }

    @Transactional
    @Override
    public PortalGameResponseDto updateGameStatus(GameType gameType, boolean active) {
        GameCatalogueEntity catalogue = catalogueRepo.findByGameType(gameType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        catalogue.setActive(active);
        catalogue = catalogueRepo.save(catalogue);
        return PortalGameResponseDto.from(catalogue, gameDetailRepo.findByCatalogue(catalogue).orElse(null));
    }

    @Transactional
    @Override
    public void deleteGame(GameType gameType) {
        GameCatalogueEntity catalogue = catalogueRepo.findByGameType(gameType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        catalogue.deactivate();
        catalogueRepo.save(catalogue);
    }

    @Transactional
    @Override
    public void deactivateGame(GameType gameType) {
        GameCatalogueEntity game = catalogueRepo.findByGameType(gameType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
        game.deactivate();
        catalogueRepo.save(game);
    }


    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
