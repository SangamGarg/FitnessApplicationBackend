package com.fitnessapp.support.repo;

import com.fitnessapp.support.models.chatBotModel.entity.ChatFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedbackEntity, UUID> {

    boolean existsByMessage_IdAndUserId(UUID messageId, String userId);
}
