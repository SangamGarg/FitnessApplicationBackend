package com.fitnessapp.support.chatbot.service;

import com.fitnessapp.support.chatbot.model.dto.request.CreateChatConversationRequest;
import com.fitnessapp.support.chatbot.model.dto.response.ChatConversationResponse;
import com.fitnessapp.support.chatbot.model.dto.response.ChatMessageResponse;
import com.fitnessapp.support.chatbot.model.dto.response.ChatPageResponse;
import com.fitnessapp.support.chatbot.model.entity.ChatConversationEntity;
import com.fitnessapp.support.chatbot.exception.ChatConversationNotFoundException;
import com.fitnessapp.support.chatbot.repository.ChatConversationRepository;
import com.fitnessapp.support.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotConversationService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    @Transactional
    public ChatConversationResponse createConversation(
            String userId, CreateChatConversationRequest request) {

        String title = request == null ? null : normalizeTitle(request.title());

        ChatConversationEntity conversation = ChatConversationEntity.builder()
                .userId(userId)
                .title(title)
                .build();

        return ChatConversationResponse.from(conversationRepository.save(conversation));
    }

    @Transactional(readOnly = true)
    public ChatPageResponse<ChatConversationResponse> getConversations(
            String userId, int page, int size) {

        Page<ChatConversationResponse> result = conversationRepository
                .findByUserIdOrderByLastMessageAtDesc(
                        userId,
                        PageRequest.of(page, size)
                )
                .map(ChatConversationResponse::from);

        return ChatPageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public ChatConversationResponse getConversation(UUID conversationId, String userId) {
        return ChatConversationResponse.from(getOwnedConversation(conversationId, userId));
    }

    @Transactional(readOnly = true)
    public ChatPageResponse<ChatMessageResponse> getMessages(
            UUID conversationId, String userId, int page, int size) {

        getOwnedConversation(conversationId, userId);

        Page<ChatMessageResponse> result = messageRepository
                .findByConversation_IdOrderByCreatedAtDesc(
                        conversationId,
                        PageRequest.of(page, size)
                )
                .map(ChatMessageResponse::from);

        // The repository returns newest-first. Reverse this page for chat display.
        var chronological = new java.util.ArrayList<>(result.getContent());
        Collections.reverse(chronological);

        Page<ChatMessageResponse> chronologicalPage =
                new org.springframework.data.domain.PageImpl<>(
                        chronological, result.getPageable(), result.getTotalElements());

        return ChatPageResponse.from(chronologicalPage);
    }

    ChatConversationEntity getOwnedConversation(UUID conversationId, String userId) {
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(ChatConversationNotFoundException::new);
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim();
    }
}
