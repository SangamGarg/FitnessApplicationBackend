package com.fitnessapp.activity.constants;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.ErrorDtoUserDetailsAndAuth;

public final class AppConstantsStartActivityService {
    public static final String SUCCESS_API_STATUS = "Success";
    public static final String ERROR_API_STATUS = "Error";
    public static final String API_PREFIX = "/api/v1/activity";


    public static ErrorDtoUserDetailsAndAuth errorDtoStartActivityService(Exception e) {
        return ErrorDtoUserDetailsAndAuth
                .builder()
                .errorImageUrl("")
                .status(AppConstantsStartActivityService.ERROR_API_STATUS)
                .errorMessage("Error : Please Try Again " + e.getLocalizedMessage())
                .build();
    }
}