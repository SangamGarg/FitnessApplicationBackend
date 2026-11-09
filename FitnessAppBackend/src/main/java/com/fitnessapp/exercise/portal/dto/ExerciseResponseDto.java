package com.fitnessapp.exercise.portal.dto;

import com.fitnessapp.exercise.app.model.ExerciseDifficulty;
import com.fitnessapp.exercise.app.model.ExerciseType;
import com.fitnessapp.exercise.app.model.MediaType;

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
        Instant createdAt,
        Instant updatedAt
) {
    public record MediaItem(Long id, MediaType type, String url, String thumbnailUrl, int sortOrder, boolean primary) {
    }
}
