package com.fitnessapp.support.chatbot.model.dto.response;

import java.time.LocalDateTime;

public record ChatApiErrorResponse(
        String message,
        LocalDateTime timestamp
) {
    public static ChatApiErrorResponse of(String message) {
        return new ChatApiErrorResponse(message, LocalDateTime.now());
    }
}
