package com.fitnessapp.gamify.gamification.service;

import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamify.achievement.service.AchievementService;
import com.fitnessapp.gamify.challenge.model.enums.ChallengeMetric;
import com.fitnessapp.gamify.challenge.service.ChallengeService;
import com.fitnessapp.gamify.gamification.model.dto.request.CreateGameRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.GameCompleteRequestDto;
import com.fitnessapp.gamify.gamification.model.dto.request.GameStartRequestDto;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
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
    public GameCatalogueResponseDto createGame(CreateGameRequestDto request) {
        if (catalogueRepo.findByGameTypeAndActiveTrue(request.gameType()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game already exists");
        }

        GameCatalogueEntity catalogue = GameCatalogueEntity.builder()
                .gameType(request.gameType())
                .title(request.title())
                .subTitle(request.subTitle())
                .description(request.description())
                .imageUrl(request.imageUrl())
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
                catalogue,
                request.gifUrl(),
                request.videoUrl(),
                request.rules() != null ? request.rules() : List.of(),
                request.tips() != null ? request.tips() : List.of()
        );

        gameDetailRepo.save(details);

        return GameCatalogueResponseDto.from(catalogue);
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
