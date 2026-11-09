package com.fitnessapp.dashboard.service;

import com.fitnessapp.activity.models.dtos.responseDto.ActivitySummaryResponseDto;
import com.fitnessapp.activity.service.ActivityInterface;
import com.fitnessapp.appAuth.model.entity.UserDetailsEntity;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserDetailsRepo;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.dashboard.dto.DashboardResponseDto;
import com.fitnessapp.exercise.portal.WorkoutPlanService;
import com.fitnessapp.exercise.app.service.WorkoutAnalyticsService;
import com.fitnessapp.exercise.app.model.dto.response.WorkoutAnalyticsOverviewResponse;
import com.fitnessapp.exercise.portal.dto.WorkoutPlanResponseDto;
import com.fitnessapp.notification.service.UserNotificationService;
import com.fitnessapp.nutrition.dto.response.DailyNutritionSummaryResponse;
import com.fitnessapp.nutrition.service.NutritionService;
import com.fitnessapp.wallet.service.WalletService;
import com.fitnessapp.wallet.model.dto.WalletBalanceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepo userRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final WalletService walletService;
    private final UserNotificationService userNotificationService;
    private final WorkoutPlanService workoutPlanService;
    private final ActivityInterface activityService;
    private final NutritionService nutritionService;
    private final WorkoutAnalyticsService workoutAnalyticsService;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<DashboardResponseDto> getDashboard(String timeZone) {
        String userId = currentUserId();
        if (!StringUtils.hasText(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user is required"
            );
        }

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZone);
        } catch (DateTimeException exception) {
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
        WalletBalanceResponseDto wallet = walletService.getBalance(userId);
        long unreadCount = userNotificationService.getMyUnreadCount().unreadCount();

        ActivitySummaryResponseDto activity = activityService.getDailySummary(
                LocalDate.now(zoneId),
                zoneId
        );

        DailyNutritionSummaryResponse nutrition = nutritionService.getDailySummary(
                userId, LocalDate.now(zoneId)
        );
        LocalDate today = LocalDate.now(zoneId);
        WorkoutAnalyticsOverviewResponse workoutAnalytics = workoutAnalyticsService.overviewForUser(
                userId, today.minusDays(6), today, zoneId
        );
        DashboardResponseDto responseDto = new DashboardResponseDto(
                toUserSummary(user, details),
                activity,
                nutrition,
                workoutAnalytics,
                new DashboardResponseDto.WalletSummary(wallet.balanceCoins()),
                new DashboardResponseDto.NotificationSummary(unreadCount),
                findRecommendedWorkout(details),
                Instant.now()
        );

        return ResponseEntity.ok().body(responseDto);
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

        var plans = workoutPlanService.search(
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
