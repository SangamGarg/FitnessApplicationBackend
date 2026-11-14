package com.fitnessapp.dashboard.dto;

import com.fitnessapp.activity.models.dtos.responseDto.ActivitySummaryResponseDto;
import com.fitnessapp.appAuth.model.enums.ActivityLevel;
import com.fitnessapp.appAuth.model.enums.FitnessGoal;
import com.fitnessapp.appAuth.model.enums.Gender;
import com.fitnessapp.nutrition.model.dto.response.DailyNutritionSummaryResponse;
import com.fitnessapp.exercise.app.model.dto.response.WorkoutAnalyticsOverviewResponse;

import java.math.BigDecimal;
import java.time.Instant;

public record DashboardResponseDto(
        UserSummary user, ActivitySummaryResponseDto activity,
        DailyNutritionSummaryResponse nutrition,
        WorkoutAnalyticsOverviewResponse workoutAnalytics,
        WalletSummary wallet, NotificationSummary notifications,
        WorkoutRecommendation recommendedWorkout, Instant generatedAt
) {
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
}
