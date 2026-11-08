package com.fitnessapp.gamification.game.service;

import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamification.game.model.GameSessionEntity;
import com.fitnessapp.gamification.game.model.dto.GameCompleteRequestDto;
import com.fitnessapp.gamification.game.model.dto.GameSessionResponseDto;
import com.fitnessapp.gamification.game.model.dto.GameStartRequestDto;
import com.fitnessapp.gamification.game.repo.GameSessionRepository;
import com.fitnessapp.gamification.game.model.GameSessionStatus;
import com.fitnessapp.gamification.game.model.GameType;
import com.fitnessapp.gamification.challenge.*;
import com.fitnessapp.gamification.achievement.AchievementService;
import com.fitnessapp.gamification.service.GamificationRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final UserRepo users;
    private final GameSessionRepository sessions;
    private final GamificationRewardService rewards;
    private final ChallengeService challenges;
    private final AchievementService achievements;
    @Value("${gamification.games.minimum-duration-seconds:10}")
    private int minDuration;
    @Value("${gamification.games.max-reps-per-minute:180}")
    private int maxRepsPerMinute;
    @Value("${gamification.games.xp-per-rep:1}")
    private long xpPerRep;
    @Value("${gamification.games.coins-per-session:5}")
    private long coinsPerSession;

    @Transactional
    public GameSessionResponseDto start(GameStartRequestDto request) {
        String userId = currentUserId();

        var user = users.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var old = sessions.findByUserIdAndClientRequestId(userId, request.clientRequestId());
        if (old.isPresent()) {
            if (old.get().getGameType() != request.gameType())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Request ID reused for another game");
            return GameSessionResponseDto.from(old.get());
        }
        return GameSessionResponseDto.from(sessions.save(new GameSessionEntity(user, request.gameType(), request.clientRequestId())));
    }

    @Transactional
    public GameSessionResponseDto complete(UUID id, GameCompleteRequestDto request) {
        String userId = currentUserId();

        users.findByIdForUpdate(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var s = sessions.findByIdAndUserId(id, userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found"));
        if (s.getStatus() == GameSessionStatus.COMPLETED) return GameSessionResponseDto.from(s);
        if (s.getStatus() != GameSessionStatus.IN_PROGRESS)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game session is not active");
        long elapsed = Duration.between(s.getStartedAt(), Instant.now()).getSeconds();
        if (elapsed < minDuration)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game session is too short");
        if (elapsed > 7200) throw new ResponseStatusException(HttpStatus.CONFLICT, "Game session expired");
        if (request.durationSeconds() > elapsed + 15 || request.durationSeconds() < Math.max(1, elapsed - 30))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submitted duration does not match server session duration");
        boolean plank = s.getGameType() == GameType.PLANK_CHALLENGE;
        int reps = plank ? 0 : request.repsDetected();
        Integer hold = plank ? request.holdSeconds() : null;
        if (plank && (hold == null || hold < minDuration || hold > elapsed + 15))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plank hold duration is invalid");
        if (!plank && reps < 1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one valid repetition is required");
        if (!plank && reps > Math.ceil((elapsed / 60.0) * maxRepsPerMinute))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repetition count exceeds the allowed rate");
        long score = plank ? Math.multiplyExact((long) hold, 2L) : Math.multiplyExact((long) reps, 10L);
        s.complete(reps, hold, (int) elapsed, score, request.trackerVersion(), request.modelConfidence());
        sessions.saveAndFlush(s);
        long xp = Math.multiplyExact(plank ? (long) hold : (long) reps, Math.max(0, xpPerRep));
        rewards.awardExternalReward(userId, "GAME_SESSION:" + s.getId(), "GAME", s.getId().toString(), xp, coinsPerSession, "Fitness game reward");
        challenges.recordProgress(userId, ChallengeMetric.GAME_REPS, reps, s.getGameType().name(), s.getId().toString());
        challenges.recordProgress(userId, ChallengeMetric.GAME_SCORE, score, s.getGameType().name(), s.getId().toString());
        if (plank)
            challenges.recordProgress(userId, ChallengeMetric.GAME_HOLD_SECONDS, hold, s.getGameType().name(), s.getId().toString());
        achievements.evaluate(userId);
        return GameSessionResponseDto.from(s);
    }

    @Transactional(readOnly = true)
    public Page<GameSessionResponseDto> history(Pageable p) {
        String userId = currentUserId();

        return sessions.findAllByUserIdOrderByStartedAtDesc(userId, p).map(GameSessionResponseDto::from);
    }

    public List<Map<String, String>> catalogue() {
        return Arrays.stream(GameType.values()).map(t -> Map.of("gameType", t.name(), "displayName", switch (t) {
            case SQUAT_CHALLENGE -> "Squat Challenge";
            case PUSH_UP_CHALLENGE -> "Push-up Challenge";
            case JUMPING_JACK_CHALLENGE -> "Jumping Jack Challenge";
            case LUNGE_CHALLENGE -> "Lunge Challenge";
            case PLANK_CHALLENGE -> "Plank Challenge";
        })).toList();
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
