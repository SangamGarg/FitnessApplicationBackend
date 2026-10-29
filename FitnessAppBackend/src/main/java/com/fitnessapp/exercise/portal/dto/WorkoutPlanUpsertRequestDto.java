package com.fitnessapp.exercise.portal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record WorkoutPlanUpsertRequestDto(
        @NotBlank @Size(max = 80) String planKey,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 10000) String description,
        @NotBlank @Size(max = 30) String goal,
        @NotBlank @Size(max = 20) String difficulty,
        @Min(1) @Max(52) int durationWeeks,
        @Min(1) @Max(7) int daysPerWeek,
        @Min(1) @Max(600) Integer estimatedSessionMinutes,
        @Size(max = 5000) String equipmentRequirementsJson,
        @NotEmpty List<@Valid WorkoutDay> days
) {
    public record WorkoutDay(
            @Positive long dayNumber,
            @NotBlank @Size(max = 100) String name,
            boolean restDay,
            @Size(max = 7) List<@Valid PlanExercise> exercises
    ) {
    }

    public record PlanExercise(
            @Positive long exerciseId,
            @Min(0) int sortOrder,
            @Min(1) @Max(100) int targetSets,
            @Min(1) Integer targetRepsMin,
            @Min(1) Integer targetRepsMax,
            @Min(1) Integer targetDurationSeconds,
            @Min(0) @Max(3600) Integer restSeconds,
            @DecimalMin("0.0") BigDecimal targetWeightKg,
            @Size(max = 2000) String notes
    ) {
    }
}
