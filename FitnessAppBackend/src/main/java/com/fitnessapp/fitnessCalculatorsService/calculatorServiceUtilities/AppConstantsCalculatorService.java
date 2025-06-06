package com.fitnessapp.fitnessCalculatorsService.calculatorServiceUtilities;


import com.fitnessapp.fitnessCalculatorsService.models.dtos.ErrorDto;

public final class AppConstantsCalculatorService {
    public static final String SUCCESS_API_STATUS = "Success";
    public static final String ERROR_API_STATUS = "Error";
    public static final String API_PREFIX = "/api/v1/calculator";


    public static ErrorDto errorDto(Exception e) {
        return ErrorDto
                .builder()
                .errorImageUrl("")
                .status(AppConstantsCalculatorService.ERROR_API_STATUS)
                .errorMessage("Error : Please Try Again " + e.getLocalizedMessage())
                .build();
    }
}
