package com.fitnessapp.email;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public final class AppConstantsEmail {
    public static final String SUCCESS_API_STATUS = "Success";
    public static final String ERROR_API_STATUS = "Error";
    public static final String API_PREFIX_EMAIL = "/api/v1/email";

    public static ErrorDto errorDto(Exception e) {
        return ErrorDto
                .builder()
                .errorImageUrl("")
                .status(AppConstantsEmail.ERROR_API_STATUS)
                .errorMessage("Error : Please Try Again" + e.getLocalizedMessage())
                .build();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class ErrorDto {
        private String status;
        private Integer statusCode;
        private String errorImageUrl;
        private String errorMessage;
    }
}
