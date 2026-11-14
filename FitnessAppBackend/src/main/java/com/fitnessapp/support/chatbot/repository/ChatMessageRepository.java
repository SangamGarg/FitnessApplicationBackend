package com.fitnessapp.support.chatbot.repository;

import com.fitnessapp.support.chatbot.model.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    Page<ChatMessageEntity> findByConversation_IdOrderByCreatedAtDesc(
            UUID conversationId, Pageable pageable);

    Optional<ChatMessageEntity> findByIdAndConversation_UserId(
            UUID messageId, String userId);

    long countByConversation_Id(UUID conversationId);
}
