package com.fitnessapp.gamify.achievement.service;

import com.fitnessapp.gamify.activity.repository.ActivityRepository;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.gamify.achievement.model.AchievementCode;
import com.fitnessapp.gamify.achievement.model.AchievementResponseDto;
import com.fitnessapp.gamify.achievement.model.UserAchievementEntity;
import com.fitnessapp.gamify.achievement.repository.UserAchievementRepository;
import com.fitnessapp.gamify.gamification.model.entity.UserStreakEntity;
import com.fitnessapp.gamify.gamification.model.enums.GameSessionStatus;
import com.fitnessapp.gamify.gamification.repository.GameSessionRepository;
import com.fitnessapp.gamify.gamification.repository.UserStreakRepository;
import com.fitnessapp.gamify.gamification.service.GamificationRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {
    private final UserAchievementRepository achievements;
    private final ActivityRepository activities;
    private final GameSessionRepository games;
    private final UserStreakRepository streaks;
    private final UserRepo users;
    private final GamificationRewardService rewardService;

    @Transactional
    public void evaluate(String userId) {
        users.findByIdForUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        long activityCount = activities.countByUser_IdAndIncludedInSummaryTrue(userId);
        long gameCount = games.countByUserIdAndStatus(userId, GameSessionStatus.COMPLETED);
        long gameReps = games.sumRepsByUser(userId);
        int longest = streaks.findByUserId(userId).map(UserStreakEntity::getLongestStreak).orElse(0);
        if (activityCount >= 1) unlock(userId, AchievementCode.FIRST_ACTIVITY, 20, 5);
        if (activityCount >= 10) unlock(userId, AchievementCode.TEN_ACTIVITIES, 50, 10);
        if (gameCount >= 1) unlock(userId, AchievementCode.FIRST_GAME, 20, 5);
        if (gameReps >= 100) unlock(userId, AchievementCode.HUNDRED_GAME_REPS, 75, 15);
        if (longest >= 7) unlock(userId, AchievementCode.SEVEN_DAY_STREAK, 100, 25);
    }

    private void unlock(String userId, AchievementCode code, long xp, long coins) {
        if (achievements.existsByUserIdAndCode(userId, code)) return;
        var user = users.findById(userId).orElseThrow();
        achievements.saveAndFlush(new UserAchievementEntity(user, code));
        rewardService.awardExternalReward(userId, "ACHIEVEMENT:" + code.name(), "ACHIEVEMENT", code.name(), xp, coins, "Achievement unlocked: " + code.name());
    }

    @Transactional(readOnly = true)
    public List<AchievementResponseDto> list() {
        String userId = currentUserId();
        Map<AchievementCode, UserAchievementEntity> unlocked = new EnumMap<>(AchievementCode.class);
        achievements.findAllByUserIdOrderByUnlockedAtDesc(userId).forEach(a -> unlocked.put(a.getCode(), a));
        return Arrays.stream(AchievementCode.values()).map(c -> {
            var a = unlocked.get(c);
            return new AchievementResponseDto(c, title(c), description(c), xp(c), coins(c), a != null, a == null ? null : a.getUnlockedAt());
        }).toList();
    }

    private String title(AchievementCode c) {
        return switch (c) {
            case FIRST_ACTIVITY -> "First Step";
            case TEN_ACTIVITIES -> "Getting Consistent";
            case FIRST_GAME -> "Game On";
            case HUNDRED_GAME_REPS -> "100 Repetitions";
            case SEVEN_DAY_STREAK -> "Week Warrior";
        };
    }

    private String description(AchievementCode c) {
        return switch (c) {
            case FIRST_ACTIVITY -> "Complete your first tracked activity.";
            case TEN_ACTIVITIES -> "Complete ten tracked activities.";
            case FIRST_GAME -> "Complete your first pose-based game.";
            case HUNDRED_GAME_REPS -> "Reach 100 repetitions across fitness games.";
            case SEVEN_DAY_STREAK -> "Maintain a seven-day activity streak.";
        };
    }

    private long xp(AchievementCode c) {
        return switch (c) {
            case FIRST_ACTIVITY, FIRST_GAME -> 20;
            case TEN_ACTIVITIES -> 50;
            case HUNDRED_GAME_REPS -> 75;
            case SEVEN_DAY_STREAK -> 100;
        };
    }

    private long coins(AchievementCode c) {
        return switch (c) {
            case FIRST_ACTIVITY, FIRST_GAME -> 5;
            case TEN_ACTIVITIES -> 10;
            case HUNDRED_GAME_REPS -> 15;
            case SEVEN_DAY_STREAK -> 25;
        };
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
