package com.fitnessapp.exercise.shared;

import com.fitnessapp.exercise.app.model.dto.ErrorDto;


public final class ExerciseUtils {

    public static final String API_PREFIX = "/api/v1/exercise";

    public static ErrorDto errorDto(Exception e) {
        return ErrorDto
                .builder()
                .errorImageUrl("")
                .status("Error")
                .statusCode(500)
                .errorMessage("Error : Please Try Again " + e.getLocalizedMessage())
                .build();
    }
}
