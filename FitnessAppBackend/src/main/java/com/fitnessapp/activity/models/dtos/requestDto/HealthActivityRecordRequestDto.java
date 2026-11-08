package com.fitnessapp.activity.models.dtos.requestDto;

import com.fitnessapp.activity.models.enums.ActivityType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record HealthActivityRecordRequestDto(
        @NotBlank @Size(max = 200) String sourceRecordId,
        @Size(max = 120) String deduplicationKey,
        @Size(max = 255) String originAppId,
        @NotNull ActivityType activityType,
        @NotBlank @Size(max = 120) String activityName,
        @NotNull Instant startedAt,
        @NotNull Instant endedAt,
        @DecimalMin("0.0") BigDecimal caloriesBurned,
        @DecimalMin("0.0") BigDecimal distanceMeters,
        @jakarta.validation.constraints.PositiveOrZero Long steps
) {
}
