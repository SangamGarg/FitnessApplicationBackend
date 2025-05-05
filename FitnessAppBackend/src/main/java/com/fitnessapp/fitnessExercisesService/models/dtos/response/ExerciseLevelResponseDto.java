package com.fitnessapp.fitnessExercisesService.models.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseLevelResponseDto {
    private Long id;
    private String level;
}
