package com.fitnessapp.fitnessExercisesService.models.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseDetailResponseDto {
    private Long id;
    private String name;
    private String description;
    private String videoUrl;
    private String imageUrl;
    private Integer durationInSeconds;
    private Integer repetitions;
    private Integer sets;
    private String category;
    private List<String> muscleGroups;
    private String equipment;
    private String difficulty;
    private List<String> benefits;
    private List<String> instructions;
    private List<String> tips;
    private Double caloriesBurned;
}
