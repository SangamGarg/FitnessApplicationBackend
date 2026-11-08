package com.fitnessapp.exercise.app.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WorkoutTrendsResponse(
        String period,
        LocalDate from,
        LocalDate to,
        List<TrendPoint> points
) {
    public record TrendPoint(
            LocalDate periodStart,
            long workouts,
            long durationSeconds,
            long sets,
            long reps,
            BigDecimal volumeKg
    ) {}
}
