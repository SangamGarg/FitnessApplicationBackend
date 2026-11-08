package com.fitnessapp.activity.models.dtos.requestDto;

import com.fitnessapp.activity.models.enums.ActivitySource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HealthActivitySyncRequestDto(
        @NotNull ActivitySource source,
        @NotEmpty @Size(max = 500) List<@Valid HealthActivityRecordRequestDto> records
) {
}
