package com.fitnessapp.dashboard.service;

import com.fitnessapp.dashboard.repo.AppBannerRepository;
import com.fitnessapp.exercise.service.ExerciseService;
import com.fitnessapp.gamify.activity.models.dtos.responseDto.ActivitySummaryResponseDto;
import com.fitnessapp.gamify.activity.service.ActivityInterface;
import com.fitnessapp.appAuth.model.entity.UserDetailsEntity;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserDetailsRepo;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.dashboard.dto.response.DashboardResponseDto;
import com.fitnessapp.exercise.model.dto.response.WorkoutAnalyticsOverviewResponse;
import com.fitnessapp.exercise.service.WorkoutAnalyticsService;
import com.fitnessapp.exercise.model.dto.response.WorkoutPlanResponseDto;
import com.fitnessapp.gamify.achievement.model.AchievementResponseDto;
import com.fitnessapp.gamify.achievement.service.AchievementService;
import com.fitnessapp.gamify.challenge.model.dto.ChallengeResponseDto;
import com.fitnessapp.gamify.challenge.model.enums.ChallengeStatus;
import com.fitnessapp.gamify.challenge.service.ChallengeService;
import com.fitnessapp.gamify.gamification.model.dto.response.GamificationHomeSummaryResponseDto;
import com.fitnessapp.gamify.gamification.service.GamificationService;
import com.fitnessapp.notification.service.UserNotificationService;
import com.fitnessapp.nutrition.model.dto.response.DailyNutritionHomeSummaryResponse;
import com.fitnessapp.nutrition.service.NutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepo userRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final UserNotificationService userNotificationService;
    private final ExerciseService exerciseService;
    private final ActivityInterface activityService;
    private final NutritionService nutritionService;
    private final WorkoutAnalyticsService workoutAnalyticsService;
    private final GamificationService gamificationService;
    private final AchievementService achievementService;
    private final ChallengeService challengeService;
    private final AppBannerRepository appBannerRepository;


    @Value("${gamification.time-zone:Asia/Kolkata}")
    private String configuredTimeZone;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponseDto getDashboard(String timeZone) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user is required"
            );
        }

        String requestedTimeZone = StringUtils.hasText(timeZone) ? timeZone.trim() : configuredTimeZone;
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(requestedTimeZone);
        } catch (DateTimeException exception) {
            if (!StringUtils.hasText(timeZone)) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Configured gamification.time-zone is invalid."
                );
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid timeZone. Use an IANA zone such as Asia/Kolkata."
            );
        }

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User account not found"
                ));

        UserDetailsEntity details = userDetailsRepo.findByUserId(userId);
        long unreadCount = userNotificationService.getMyUnreadCount().unreadCount();

        LocalDate today = LocalDate.now(zoneId);
        ActivitySummaryResponseDto activity = activityService.getDailySummary(today, zoneId);

        DailyNutritionHomeSummaryResponse nutrition = nutritionService.getNutritionHomeSummary(today);
        WorkoutAnalyticsOverviewResponse workoutAnalytics = workoutAnalyticsService.overviewForUser(
                userId, today.minusDays(6), today, zoneId
        );
        GamificationHomeSummaryResponseDto gamification =
                gamificationService.getGamificationHomeSummary();
        List<AchievementResponseDto> achievements = achievementService.list();
        List<AchievementResponseDto> recentAchievements = achievements.stream()
                .filter(AchievementResponseDto::unlocked)
                .limit(3)
                .toList();
        List<ChallengeResponseDto> joinedChallenges = challengeService.mine();
        List<ChallengeResponseDto> activeChallenges = joinedChallenges.stream()
                .filter(challenge -> challenge.joined() && challenge.status() == ChallengeStatus.ACTIVE)
                .limit(3)
                .toList();

        DashboardResponseDto responseDto = new DashboardResponseDto(getActiveBanners(),
                toUserSummary(user, details),
                activity,
                nutrition,
                workoutAnalytics,
                new DashboardResponseDto.WalletSummary(gamification.coins()),
                new DashboardResponseDto.NotificationSummary(unreadCount),
                findRecommendedWorkout(details),
                new DashboardResponseDto.GamificationSummary(
                        gamification.totalXp(),
                        gamification.level(),
                        gamification.coins(),
                        gamification.currentStreak(),
                        gamification.longestStreak(),
                        gamification.lastActiveDate(),
                        gamification.xpToNextLevel()
                ),
                new DashboardResponseDto.AchievementSummary(
                        (int) achievements.stream().filter(AchievementResponseDto::unlocked).count(),
                        achievements.size(),
                        recentAchievements
                ),
                new DashboardResponseDto.ChallengeSummary(activeChallenges.size(), activeChallenges),
                Instant.now()
        );

        return responseDto;
    }

    private List<DashboardResponseDto.AppBannerResponseDto> getActiveBanners() {

        Instant now = Instant.now();

        return appBannerRepository.findCurrentlyActive(now)
                .stream()
                .map(banner -> new DashboardResponseDto.AppBannerResponseDto(
                        banner.getId(),
                        banner.getBannerKey(),
                        banner.getTitle(),
                        banner.getSubTitle(),
                        banner.getDescription(),
                        banner.getImageUrl(),
                        banner.getMobileImageUrl(),
                        banner.getActionLabel(),
                        banner.getActionType(),
                        banner.getActionValue(),
                        banner.getDisplayOrder(),
                        banner.isDismissible()
                ))
                .toList();
    }

    private DashboardResponseDto.UserSummary toUserSummary(
            UserEntity user,
            UserDetailsEntity details
    ) {
        if (details == null) {
            return new DashboardResponseDto.UserSummary(
                    user.getId(), user.getEmail(), null, null,
                    null, null, null, null, null, null, false
            );
        }

        boolean profileCompleted =
                StringUtils.hasText(details.getName())
                        && details.getGender() != null
                        && details.getAge() != null
                        && details.getHeightCm() != null
                        && details.getWeightKg() != null
                        && details.getActivityLevel() != null
                        && details.getGoal() != null;

        return new DashboardResponseDto.UserSummary(
                user.getId(),
                user.getEmail(),
                details.getName(),
                details.getProfileImageUrl(),
                details.getGender(),
                details.getAge(),
                details.getHeightCm(),
                details.getWeightKg(),
                details.getActivityLevel(),
                details.getGoal(),
                profileCompleted
        );
    }

    private DashboardResponseDto.WorkoutRecommendation findRecommendedWorkout(
            UserDetailsEntity details
    ) {
        if (details == null || details.getGoal() == null) {
            return null;
        }

        var plans = exerciseService.getPublishedWorkoutPlansList(
                null,
                details.getGoal().name(),
                null,
                true,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "name"))
        );

        return plans.getContent()
                .stream()
                .findFirst()
                .map(this::toWorkoutRecommendation)
                .orElse(null);
    }

    private DashboardResponseDto.WorkoutRecommendation toWorkoutRecommendation(
            WorkoutPlanResponseDto plan
    ) {
        return new DashboardResponseDto.WorkoutRecommendation(
                plan.id(),
                plan.planKey(),
                plan.name(),
                plan.description(),
                plan.goal(),
                plan.difficulty(),
                plan.estimatedSessionMinutes(),
                plan.daysPerWeek()
        );
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
