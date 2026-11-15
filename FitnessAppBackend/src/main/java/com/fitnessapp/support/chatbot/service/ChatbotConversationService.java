package com.fitnessapp.support.chatbot.service;

import com.fitnessapp.support.chatbot.exception.ChatConversationNotFoundException;
import com.fitnessapp.support.chatbot.model.dto.request.CreateChatConversationRequest;
import com.fitnessapp.support.chatbot.model.dto.response.ChatConversationResponse;
import com.fitnessapp.support.chatbot.model.dto.response.ChatMessageResponse;
import com.fitnessapp.support.chatbot.model.dto.response.ChatPageResponse;
import com.fitnessapp.support.chatbot.model.entity.ChatConversationEntity;
import com.fitnessapp.support.chatbot.repository.ChatConversationRepository;
import com.fitnessapp.support.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotConversationService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    @Transactional
    public ChatConversationResponse createConversation(
            CreateChatConversationRequest request) {
        String userId = currentUserId();

        String title = request == null ? null : normalizeTitle(request.title());

        ChatConversationEntity conversation = ChatConversationEntity.builder()
                .userId(userId)
                .title(title)
                .build();

        return ChatConversationResponse.from(conversationRepository.save(conversation));
    }

    @Transactional(readOnly = true)
    public ChatPageResponse<ChatConversationResponse> getConversations(
            int page, int size) {
        String userId = currentUserId();

        Page<ChatConversationResponse> result = conversationRepository
                .findByUserIdOrderByLastMessageAtDesc(
                        userId,
                        PageRequest.of(page, size)
                )
                .map(ChatConversationResponse::from);

        return ChatPageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public ChatConversationResponse getConversation(UUID conversationId) {
        return ChatConversationResponse.from(getOwnedConversation(conversationId));
    }

    @Transactional(readOnly = true)
    public ChatPageResponse<ChatMessageResponse> getMessages(
            UUID conversationId, int page, int size) {

        getOwnedConversation(conversationId);

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

    ChatConversationEntity getOwnedConversation(UUID conversationId) {
        String userId = currentUserId();
        return conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(ChatConversationNotFoundException::new);
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim();
    }


    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthorized access attempt without valid authentication principal in SecurityContext");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
