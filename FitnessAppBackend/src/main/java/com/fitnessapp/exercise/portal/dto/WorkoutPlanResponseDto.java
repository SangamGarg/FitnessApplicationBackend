package com.fitnessapp.exercise.portal.dto;

import java.time.Instant;
import java.util.List;

public record WorkoutPlanResponseDto(
        Long id,
        String planKey,
        String name,
        String description,
        String goal,
        String difficulty,
        int durationWeeks,
        int daysPerWeek,
        Integer estimatedSessionMinutes,
        String equipmentRequirementsJson,
        boolean published,
        int version,
        List<Day> days,
        Instant createdAt,
        Instant updatedAt
) {
    public record Day(
            int dayNumber,
            String name,
            boolean restDay,
            List<PlanExercise> exercises
    ) {
    }

    public record PlanExercise(
            Long exerciseId,
            String exerciseKey,
            String exerciseName,
            int sortOrder,
            int targetSets,
            Integer targetRepsMin,
            Integer targetRepsMax,
            Integer targetDurationSeconds,
            Integer restSeconds,
            java.math.BigDecimal targetWeightKg,
            String notes
    ) {
    }
}
