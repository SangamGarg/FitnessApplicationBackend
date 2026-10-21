package com.fitnessapp.portal.dto;


import java.time.Instant;

public record ApiErrorResponseDto(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public static ApiErrorResponseDto of(int status, String error, String message, String path) {
        return new ApiErrorResponseDto(status, error, message, path, Instant.now());
    }
}