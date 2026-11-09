package com.fitnessapp.exercise.app.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkoutAnalyticsOverviewResponse(
        LocalDate from,
        LocalDate to,
        long totalWorkouts,
        long totalDurationSeconds,
        long averageDurationSeconds,
        long totalSets,
        long totalReps,
        BigDecimal totalVolumeKg,
        long activeDays
) {}
