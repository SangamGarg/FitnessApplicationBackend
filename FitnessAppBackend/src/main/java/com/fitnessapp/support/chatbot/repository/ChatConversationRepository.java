package com.fitnessapp.support.chatbot.repository;

import com.fitnessapp.support.chatbot.model.entity.ChatConversationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatConversationRepository
        extends JpaRepository<ChatConversationEntity, UUID> {

    Optional<ChatConversationEntity> findByIdAndUserId(UUID id, String userId);

    Page<ChatConversationEntity> findByUserIdOrderByLastMessageAtDesc(
            String userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM ChatConversationEntity c
            WHERE c.id = :conversationId
              AND c.userId = :userId
            """)
    Optional<ChatConversationEntity> findByIdAndUserIdForUpdate(
            @Param("conversationId") UUID conversationId,
            @Param("userId") String userId
    );

}
