package com.fitnessapp.support.chatbot.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 3000, message = "Message cannot exceed 3000 characters")
        String message
) {
}
