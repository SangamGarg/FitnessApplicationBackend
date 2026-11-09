package com.fitnessapp.exercise.app.model.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PersonalRecordResponse(
        Long exerciseId,
        String exerciseKey,
        String exerciseName,
        RecordValue maxWeight,
        RecordValue maxReps,
        RecordValue maxDuration,
        RecordValue estimatedOneRepMax
) {
    public record RecordValue(BigDecimal value, String unit, Instant achievedAt) {}
}
