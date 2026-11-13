package com.fitnessapp.support.chatbot.dto.response;

import java.util.UUID;

public record SendChatMessageResponse(
        UUID conversationId,
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage
) {
}
