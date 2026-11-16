package com.fitnessapp.exercise.model.dto.request;

import com.fitnessapp.exercise.model.enums.TrackingMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkoutExerciseAddRequest(
    @NotNull @Positive Long exerciseId,
    @NotNull TrackingMethod trackingMethod,
    String trackerVersion
) {}
