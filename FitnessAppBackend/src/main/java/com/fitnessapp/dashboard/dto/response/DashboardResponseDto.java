package com.fitnessapp.dashboard.dto.response;

import com.fitnessapp.dashboard.dto.BannerActionType;
import com.fitnessapp.gamify.activity.models.dtos.responseDto.ActivitySummaryResponseDto;
import com.fitnessapp.appAuth.model.enums.ActivityLevel;
import com.fitnessapp.appAuth.model.enums.FitnessGoal;
import com.fitnessapp.appAuth.model.enums.Gender;
import com.fitnessapp.nutrition.model.dto.response.DailyNutritionHomeSummaryResponse;
import com.fitnessapp.exercise.model.dto.response.WorkoutAnalyticsOverviewResponse;
import com.fitnessapp.gamify.achievement.model.AchievementResponseDto;
import com.fitnessapp.gamify.challenge.model.dto.ChallengeResponseDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponseDto(
        List<AppBannerResponseDto> banners,
        UserSummary user, ActivitySummaryResponseDto activity,
        DailyNutritionHomeSummaryResponse nutrition,
        WorkoutAnalyticsOverviewResponse workoutAnalytics,
        WalletSummary wallet, NotificationSummary notifications,
        WorkoutRecommendation recommendedWorkout, GamificationSummary gamification,
        AchievementSummary achievements, ChallengeSummary challenges,
        Instant generatedAt
) {
    public record AppBannerResponseDto(
            Long id,
            String bannerKey,
            String title,
            String subTitle,
            String description,
            String imageUrl,
            String mobileImageUrl,
            String actionLabel,
            BannerActionType actionType,
            String actionValue,
            Integer displayOrder,
            boolean dismissible) {
    }

    public record UserSummary(String userId, String email, String name, String profileImageUrl,
                              Gender gender, Integer age, BigDecimal heightCm, BigDecimal weightKg,
                              ActivityLevel activityLevel, FitnessGoal goal, boolean profileCompleted) {
    }

    public record WalletSummary(long balanceCoins) {
    }

    public record NotificationSummary(long unreadCount) {
    }

    public record WorkoutRecommendation(Long id, String planKey, String name, String description,
                                        String goal, String difficulty, Integer estimatedSessionMinutes,
                                        int daysPerWeek) {
    }

    public record GamificationSummary(long totalXp, int level, long coins,
                                      int currentStreak, int longestStreak,
                                      LocalDate lastActiveDate, long xpToNextLevel) {
    }

    public record AchievementSummary(int unlockedCount, int totalCount,
                                     List<AchievementResponseDto> recentUnlocked) {
    }

    public record ChallengeSummary(int activeCount, List<ChallengeResponseDto> challenges) {
    }
}
