package com.fitnessapp.support.repo;

import com.fitnessapp.support.models.chatBotModel.entity.ChatFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatFeedbackRepository extends JpaRepository<ChatFeedbackEntity, UUID> {

    boolean existsByMessage_IdAndUserId(UUID messageId, String userId);
}
