package com.fitnessapp.config.exceptionConfig;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponseDto(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        Map<String, String> details
) {
    public static ApiErrorResponseDto of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ApiErrorResponseDto(status, error, message, path, Instant.now(), Map.of());
    }

    public static ApiErrorResponseDto of(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> details
    ) {
        return new ApiErrorResponseDto(status, error, message, path, Instant.now(), details);
    }
}
