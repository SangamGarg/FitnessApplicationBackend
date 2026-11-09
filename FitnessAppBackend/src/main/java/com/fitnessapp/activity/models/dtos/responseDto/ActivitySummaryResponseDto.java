package com.fitnessapp.activity.models.dtos.responseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivitySummaryResponseDto(
        LocalDate date,
        String timeZone,
        int activeMinutes,
        BigDecimal caloriesBurned,
        BigDecimal distanceMeters,
        long steps,
        long completedActivities,
        int dailyGoalMinutes,
        int goalProgressPercent
) {
}
