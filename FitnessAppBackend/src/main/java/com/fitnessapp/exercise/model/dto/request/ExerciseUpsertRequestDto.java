package com.fitnessapp.exercise.model.dto.request;

import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

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
        @Size(max = 20000) String trackingConfigJson,
        @Size(max = 20) List<@NotNull @Valid MuscleGroupAssignment> muscles,
        boolean clearMedia,
        @Valid CategoryAssignment category,
        @Valid SubCategoryAssignment subCategory,
        @Valid ExerciseDetailRequest detail,
        boolean clearClassification,
        boolean clearDetail
) {
    public record MuscleGroupAssignment(
            @NotBlank @Size(max = 80) String muscleKey,
            @NotBlank @Size(max = 20) String role
    ) {
    }

    public record CategoryAssignment(
            @NotBlank @Size(max = 80) String categoryKey,
            @NotBlank @Size(max = 100) String name
    ) {
    }

    public record SubCategoryAssignment(
            @NotBlank @Size(max = 80) String subCategoryKey,
            @NotBlank @Size(max = 100) String name
    ) {
    }

    public record ExerciseDetailRequest(
            @Size(max = 10000) String setup,
            @Size(max = 10000) String benefits,
            @Size(max = 10000) String safetyNotes,
            @Size(max = 10000) String commonMistakes
    ) {
    }
}
