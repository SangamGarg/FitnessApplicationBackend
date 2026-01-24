package com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities;

import com.fitnessapp.userDetailsAndAuthService.models.dtos.ErrorDtoUserDetailsAndAuth;

public final class AppConstantsUserAndAuthService {
    public static final String SUCCESS_API_STATUS = "Success";
    public static final String ERROR_API_STATUS = "Error";
    public static final String API_PREFIX = "/api/v1/user";


    public static ErrorDtoUserDetailsAndAuth errorDtoUserDetailAndAuth(Exception e) {
        return ErrorDtoUserDetailsAndAuth
                .builder()
                .errorImageUrl("")
                .status(AppConstantsUserAndAuthService.ERROR_API_STATUS)
                .errorMessage("Error : Please Try Again " + e.getLocalizedMessage())
                .build();
    }
}