package com.fitnessapp.support.chatbot.model.dto.request;

import jakarta.validation.constraints.Size;

public record CreateChatConversationRequest(
        @Size(max = 200, message = "Conversation title cannot exceed 200 characters")
        String title
) {
}
