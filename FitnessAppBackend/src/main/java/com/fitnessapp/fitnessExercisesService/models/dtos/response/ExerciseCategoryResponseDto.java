package com.fitnessapp.fitnessExercisesService.models.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseCategoryResponseDto {
    private Long id;
    private String name;
    private String imageUrl;
}