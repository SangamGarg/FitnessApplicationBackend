package com.fitnessapp.exercise.app.model.dto.request;

import com.fitnessapp.exercise.app.model.TrackingMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkoutExerciseAddRequest(
    @NotNull @Positive Long exerciseId,
    @NotNull TrackingMethod trackingMethod,
    String trackerVersion
) {}
