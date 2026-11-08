package com.fitnessapp.activity.models.dtos.requestDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record CompleteActivityRequestDto(
        @NotNull Instant endedAt,
        @DecimalMin("0.0") BigDecimal caloriesBurned,
        @DecimalMin("0.0") BigDecimal distanceMeters,
        @PositiveOrZero Long steps
) {
}
