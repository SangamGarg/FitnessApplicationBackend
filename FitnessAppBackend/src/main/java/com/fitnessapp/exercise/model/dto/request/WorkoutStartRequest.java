package com.fitnessapp.exercise.model.dto.request;

import com.fitnessapp.exercise.model.enums.WorkoutSessionMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record WorkoutStartRequest(
    @NotNull UUID clientRequestId,
    @NotNull WorkoutSessionMode sessionMode,
    @Positive Long workoutPlanId,
    @Positive Long planDayId
) {}
