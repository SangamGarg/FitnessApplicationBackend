package com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.ErrorDto;

public final class AppConstantsUserAndAuthService {
    public static final String SUCCESS_API_STATUS = "Success";
    public static final String ERROR_API_STATUS = "Error";
    public static final String API_PREFIX = "/api/v1/user";


    public static ErrorDto errorDto(Exception e) {
        return ErrorDto
                .builder()
                .errorImageUrl("")
                .status(AppConstantsUserAndAuthService.ERROR_API_STATUS)
                .errorMessage("Error : Please Try Again " + e.getLocalizedMessage())
                .build();
    }
}