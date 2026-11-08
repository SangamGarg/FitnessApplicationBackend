package com.fitnessapp.exercise.app.model.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ExercisePerformanceHistoryResponse(
        Long exerciseId,
        String exerciseKey,
        String exerciseName,
        List<PerformancePoint> history,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record PerformancePoint(
            Long workoutId,
            Instant completedAt,
            Integer workoutDurationSeconds,
            int sets,
            long totalReps,
            BigDecimal volumeKg,
            BigDecimal bestWeightKg,
            BigDecimal estimatedOneRepMaxKg
    ) {}
}
