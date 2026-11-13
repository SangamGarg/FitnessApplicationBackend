
package com.fitnessapp.support.chatbot.repository;

import com.fitnessapp.support.chatbot.entity.ChatFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatFeedbackRepository
        extends JpaRepository<ChatFeedbackEntity, UUID> {

    boolean existsByMessage_IdAndUserId(
            UUID messageId,
            String userId
    );
}
