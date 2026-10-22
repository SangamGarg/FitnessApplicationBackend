package com.fitnessapp.support.utils;

import com.fitnessapp.support.models.dtos.ErrorDtoSupport;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;

public final class SupportUtils {


    public static final String SUCCESS_API_STATUS = "Success";
    public static final String ERROR_API_STATUS = "Error";
    public static final String API_PREFIX = "/api/v1/support";


    public static ErrorDtoSupport errorDtoSupportService(Exception e) {
        return ErrorDtoSupport
                .builder()
                .errorImageUrl("")
                .status(AppConstantsUserAndAuthService.ERROR_API_STATUS)
                .errorMessage("Error : Please Try Again " + e.getLocalizedMessage())
                .build();
    }
}
