package com.fitnessapp.support.repo;

import com.fitnessapp.support.models.chatBotModel.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    Page<ChatMessageEntity> findByConversation_IdOrderByCreatedAtDesc(
            UUID conversationId, Pageable pageable);

    Optional<ChatMessageEntity> findByIdAndConversation_UserId(
            UUID messageId, String userId);

    long countByConversation_Id(UUID conversationId);
}
