package com.fitnessapp.support.models.chatBotModel.dto.response;

import com.fitnessapp.support.models.chatBotModel.entity.ChatConversationEntity;
import com.fitnessapp.support.models.chatBotModel.enums.ChatConversationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatConversationResponse(
        UUID id,
        String title,
        ChatConversationStatus status,
        String linkedTicketNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastMessageAt
) {
    public static ChatConversationResponse from(ChatConversationEntity entity) {
        return new ChatConversationResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getLinkedTicketNumber(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastMessageAt()
        );
    }
}
