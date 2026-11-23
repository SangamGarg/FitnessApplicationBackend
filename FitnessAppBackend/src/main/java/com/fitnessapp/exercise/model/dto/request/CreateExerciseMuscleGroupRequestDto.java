package com.fitnessapp.exercise.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExerciseMuscleGroupRequestDto(
        @NotBlank @Size(max = 80) String muscleKey,
        @NotBlank @Size(max = 100) String name
) {
}
