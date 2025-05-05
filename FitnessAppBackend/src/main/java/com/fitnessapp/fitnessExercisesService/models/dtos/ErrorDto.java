package com.fitnessapp.fitnessExercisesService.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ErrorDto {
    private String status;
    private Integer statusCode;
    private String errorImageUrl;
    private String errorMessage;
}

