package com.fitnessapp.activity.models.dtos.requestDto;

import com.fitnessapp.activity.models.enums.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StartActivityRequestDto(
        @NotNull UUID clientActivityId,
        @NotNull ActivityType activityType,
        @NotBlank @Size(max = 120) String activityName
) {
}
