package com.fitnessapp.fitnessExercisesService.exerciseServiceUtilities;

import com.fitnessapp.fitnessExercisesService.models.dtos.ErrorDto;


public final class AppConstantsExerciseService {
    public static final String SUCCESS_API_STATUS = "Success";
    public static final String ERROR_API_STATUS = "Error";
    public static final String API_PREFIX = "/api/v1/exercise";

    public static ErrorDto errorDto(Exception e) {
        return ErrorDto
                .builder()
                .errorImageUrl("")
                .status(AppConstantsExerciseService.ERROR_API_STATUS)
                .statusCode(500)
                .errorMessage("Error : Please Try Again " + e.getLocalizedMessage())
                .build();
    }
}
