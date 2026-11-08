package com.fitnessapp.exercise.app.model.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WorkoutSetLogRequest(
    @Min(1) @Max(100) short setNumber,
    @Positive Integer repsCompleted,
    @DecimalMin("0.00") @DecimalMax("2000.00") BigDecimal weightKg,
    @Positive Integer durationSeconds,
    @Min(0) @Max(3600) Integer restSeconds,
    @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal repConfidence
) {}
