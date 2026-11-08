package com.fitnessapp.exercise.app.model.dto.response;

import com.fitnessapp.exercise.app.model.WorkoutSessionMode;
import com.fitnessapp.exercise.app.model.WorkoutStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record WorkoutSessionResponse(
    Long id, Long workoutPlanId, Long planDayId, WorkoutSessionMode sessionMode,
    WorkoutStatus status, Instant startedAt, Instant completedAt,
    Integer durationSeconds, List<ExerciseLog> exercises
) {
    public record ExerciseLog(Long id, Long exerciseId, String exerciseKey, String exerciseName,
        int sortOrder, String status, String trackingMethod, String trackerVersion,
        List<SetLog> sets) {}
    public record SetLog(Long id, int setNumber, Integer repsCompleted, BigDecimal weightKg,
        Integer durationSeconds, Integer restSeconds, BigDecimal repConfidence, Instant completedAt) {}
}
