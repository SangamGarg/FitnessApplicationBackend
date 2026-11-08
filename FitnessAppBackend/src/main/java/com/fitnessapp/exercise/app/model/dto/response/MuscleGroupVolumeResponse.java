package com.fitnessapp.exercise.app.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MuscleGroupVolumeResponse(
        LocalDate from,
        LocalDate to,
        List<MuscleGroupMetric> muscleGroups
) {
    public record MuscleGroupMetric(
            Long muscleGroupId,
            String muscleGroupKey,
            String muscleGroupName,
            long sets,
            long reps,
            BigDecimal volumeKg,
            long workoutFrequency
    ) {}
}
