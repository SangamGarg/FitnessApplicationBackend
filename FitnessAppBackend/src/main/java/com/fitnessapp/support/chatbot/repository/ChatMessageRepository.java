
package com.fitnessapp.support.chatbot.repository;

import com.fitnessapp.support.chatbot.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessageEntity, UUID> {

    Page<ChatMessageEntity> findByConversation_IdOrderByCreatedAtDesc(
            UUID conversationId,
            Pageable pageable
    );

    long countByConversation_Id(UUID conversationId);
}
