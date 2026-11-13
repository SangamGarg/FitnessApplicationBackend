package com.fitnessapp.support.chatbot.dto.response;

import java.time.LocalDateTime;

public record ChatApiErrorResponse(
        String message,
        LocalDateTime timestamp
) {
    public static ChatApiErrorResponse of(String message) {
        return new ChatApiErrorResponse(message, LocalDateTime.now());
    }
}
