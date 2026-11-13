package com.fitnessapp.support.chatbot.service;

import com.fitnessapp.support.chatbot.entity.ChatConversationEntity;
import com.fitnessapp.support.chatbot.entity.ChatFeedbackEntity;
import com.fitnessapp.support.chatbot.entity.ChatMessageEntity;
import com.fitnessapp.support.chatbot.enums.ChatConversationStatus;
import com.fitnessapp.support.chatbot.enums.ChatFeedbackRating;
import com.fitnessapp.support.chatbot.enums.ChatSenderType;
import com.fitnessapp.support.chatbot.repository.ChatConversationRepository;
import com.fitnessapp.support.chatbot.repository.ChatFeedbackRepository;
import com.fitnessapp.support.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotMessagePersistenceService {

    private static final int CONTEXT_MESSAGE_LIMIT = 12;
    private static final int CONTEXT_CHARACTER_LIMIT = 12000;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatFeedbackRepository feedbackRepository;

    @Transactional
    public ChatMessageEntity saveUserMessage(
            UUID conversationId, String userId, String messageText) {

        ChatConversationEntity conversation = getActiveConversation(conversationId, userId);

        ChatMessageEntity message = ChatMessageEntity.builder()
                .conversation(conversation)
                .senderType(ChatSenderType.USER)
                .message(messageText.trim())
                .build();

        ChatMessageEntity saved = messageRepository.save(message);

        if (conversation.getTitle() == null || conversation.getTitle().isBlank()) {
            conversation.setTitle(createTitle(messageText));
        }

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return saved;
    }

    @Transactional(readOnly = true)
    public String loadRecentContext(UUID conversationId, String userId) {
        getOwnedConversation(conversationId, userId);

        List<ChatMessageEntity> messages = new java.util.ArrayList<>(
                messageRepository.findByConversation_IdOrderByCreatedAtDesc(
                        conversationId, PageRequest.of(0, CONTEXT_MESSAGE_LIMIT))
                        .getContent());

        Collections.reverse(messages);

        StringBuilder context = new StringBuilder();
        for (ChatMessageEntity message : messages) {
            String role = message.getSenderType() == ChatSenderType.ASSISTANT
                    ? "Assistant" : "User";
            context.append(role)
                    .append(": ")
                    .append(message.getMessage())
                    .append('\n');
        }

        if (context.length() > CONTEXT_CHARACTER_LIMIT) {
            return context.substring(context.length() - CONTEXT_CHARACTER_LIMIT);
        }

        return context.toString();
    }

    @Transactional
    public ChatMessageEntity saveAssistantMessage(
            UUID conversationId, String userId, String answer) {

        ChatConversationEntity conversation = getActiveConversation(conversationId, userId);

        ChatMessageEntity message = ChatMessageEntity.builder()
                .conversation(conversation)
                .senderType(ChatSenderType.ASSISTANT)
                .message(answer)
                .build();

        ChatMessageEntity saved = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return saved;
    }

    @Transactional
    public void submitFeedback(
            UUID messageId, String userId, ChatFeedbackRating rating, String feedbackText) {

        ChatMessageEntity message = messageRepository
                .findByIdAndConversation_UserId(messageId, userId)
                .orElseThrow(ChatMessageNotFoundException::new);

        if (message.getSenderType() != ChatSenderType.ASSISTANT) {
            throw new IllegalArgumentException(
                    "Feedback can only be submitted for assistant messages");
        }

        if (feedbackRepository.existsByMessage_IdAndUserId(messageId, userId)) {
            throw new IllegalStateException("Feedback has already been submitted");
        }

        ChatFeedbackEntity feedback = ChatFeedbackEntity.builder()
                .message(message)
                .userId(userId)
                .rating(rating)
                .feedbackText(feedbackText == null || feedbackText.isBlank()
                        ? null : feedbackText.trim())
                .build();

        feedbackRepository.save(feedback);
    }

    private ChatConversationEntity getActiveConversation(UUID conversationId, String userId) {
        ChatConversationEntity conversation = getOwnedConversation(conversationId, userId);

        if (conversation.getStatus() != ChatConversationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "This conversation is no longer active");
        }

        return conversation;
    }

    private ChatConversationEntity getOwnedConversation(UUID conversationId, String userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(ChatConversationNotFoundException::new);
    }

    private String createTitle(String message) {
        String normalized = message == null ? "" : message.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= 60) {
            return normalized;
        }
        return normalized.substring(0, 57) + "...";
    }
}
