package com.fitnessapp.exercise.model.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateExerciseMuscleGroupRequestDto(
        @Size(max = 80) String muscleKey,
        @Size(max = 100) String name
) {
}
