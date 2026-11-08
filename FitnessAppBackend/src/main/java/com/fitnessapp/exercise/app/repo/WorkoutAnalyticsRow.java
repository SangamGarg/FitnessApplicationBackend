package com.fitnessapp.exercise.app.repo;

import java.math.BigDecimal;
import java.time.Instant;

/** Read-only projection of completed workout sets. One row is returned per set/muscle mapping. */
public interface WorkoutAnalyticsRow {
    Long getWorkoutId();
    Instant getCompletedAt();
    Integer getWorkoutDurationSeconds();
    Long getExerciseLogId();
    Long getSetId();
    Long getExerciseId();
    String getExerciseKey();
    String getExerciseName();
    Long getMuscleGroupId();
    String getMuscleGroupKey();
    String getMuscleGroupName();
    String getMuscleRole();
    Integer getSetNumber();
    Integer getRepsCompleted();
    BigDecimal getWeightKg();
    Integer getSetDurationSeconds();
}
