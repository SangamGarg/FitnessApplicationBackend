package com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BurnedCaloriesFromActivityRequestDto {

    @NotBlank(message = "Activity must not be blank")
    private String activityName;

    @Positive(message = "Weight must be greater than 0")
    @NotNull(message = "Weight must not be null")
    @Max(value = 300, message = "Weight must be at most 300kg")
    private Float weightKg;

    @NotNull(message = "Duration must not be null")
    @Positive(message = "Duration must be greater than 0")
    private Integer durationMin;
}
