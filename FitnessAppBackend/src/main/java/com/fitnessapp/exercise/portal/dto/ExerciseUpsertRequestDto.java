package com.fitnessapp.exercise.portal.dto;

import com.fitnessapp.exercise.app.model.ExerciseDifficulty;
import com.fitnessapp.exercise.app.model.ExerciseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExerciseUpsertRequestDto(
        @NotBlank @Size(max = 80) String exerciseKey,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 10000) String description,
        @Size(max = 20000) String instructionsJson,
        @NotNull ExerciseDifficulty difficulty,
        @NotNull ExerciseType exerciseType,
        @Size(max = 40) String movementPattern,
        @NotBlank @Size(max = 40) String equipment,
        boolean unilateral,
        boolean supportsAiTracking,
        @Size(max = 20000) String trackingConfigJson
) {
}
