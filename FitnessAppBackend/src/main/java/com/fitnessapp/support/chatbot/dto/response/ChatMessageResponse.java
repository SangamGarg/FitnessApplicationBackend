package com.fitnessapp.support.chatbot.dto.response;

import com.fitnessapp.support.chatbot.entity.ChatMessageEntity;
import com.fitnessapp.support.chatbot.enums.ChatSenderType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        ChatSenderType senderType,
        String message,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessageEntity entity) {
        return new ChatMessageResponse(
                entity.getId(),
                entity.getSenderType(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }
}
