package com.fitnessapp.exercise.app.model.dto.response;

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
