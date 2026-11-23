package com.fitnessapp.exercise.model.dto.response;

import com.fitnessapp.exercise.model.enums.ExerciseDifficulty;
import com.fitnessapp.exercise.model.enums.ExerciseType;
import com.fitnessapp.exercise.model.enums.MediaType;

import java.time.Instant;
import java.util.List;

public record ExerciseResponseDto(
        Long id,
        String exerciseKey,
        String name,
        String description,
        String instructionsJson,
        ExerciseDifficulty difficulty,
        ExerciseType exerciseType,
        String movementPattern,
        String equipment,
        boolean unilateral,
        boolean supportsAiTracking,
        String trackingConfigJson,
        boolean published,
        List<MediaItem> media,
        List<ExerciseMuscleList> exerciseMuscles,
        CategoryItem category,
        SubCategoryItem subCategory,
        LevelItem level,
        ExerciseDetailItem detail,
        Instant createdAt,
        Instant updatedAt
) {
    public record MediaItem(Long id, MediaType type, String url, String thumbnailUrl, int sortOrder, boolean primary) {
    }

    public record CategoryItem(String categoryKey, String name) {
    }

    public record SubCategoryItem(String subCategoryKey, String name) {
    }

    public record LevelItem(String levelKey, String name, int sortOrder) {
    }

    public record ExerciseDetailItem(String setup, String benefits, String safetyNotes, String commonMistakes) {
    }

    public record ExerciseMuscleList(String role, MuscleGroupItem muscleGroup) {
        public record MuscleGroupItem(String muscleKey, String name) {
        }
    }
}
