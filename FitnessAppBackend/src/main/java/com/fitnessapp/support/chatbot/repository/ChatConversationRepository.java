
package com.fitnessapp.support.chatbot.repository;

import com.fitnessapp.support.chatbot.entity.ChatConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatConversationRepository
        extends JpaRepository<ChatConversationEntity, UUID> {

    Optional<ChatConversationEntity> findByIdAndUserId(
            UUID id,
            String userId
    );

    Page<ChatConversationEntity> findByUserIdOrderByLastMessageAtDesc(
            String userId,
            Pageable pageable
    );
}
