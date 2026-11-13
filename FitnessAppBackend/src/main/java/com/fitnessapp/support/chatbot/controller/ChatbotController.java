package com.fitnessapp.support.chatbot.controller;

import com.fitnessapp.support.chatbot.dto.request.CreateChatConversationRequest;
import com.fitnessapp.support.chatbot.dto.request.EscalateChatRequest;
import com.fitnessapp.support.chatbot.dto.request.SendChatMessageRequest;
import com.fitnessapp.support.chatbot.dto.request.SubmitChatFeedbackRequest;
import com.fitnessapp.support.chatbot.dto.response.*;
import com.fitnessapp.support.chatbot.service.ChatbotConversationService;
import com.fitnessapp.support.chatbot.service.ChatbotMessagePersistenceService;
import com.fitnessapp.support.chatbot.service.ChatbotMessageService;
import com.fitnessapp.support.chatbot.service.ChatbotTicketEscalationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_SUPPORT_ENDPOINT;

@RestController
@RequestMapping(APP_SUPPORT_ENDPOINT + "/chat")
@RequiredArgsConstructor
@Validated
public class ChatbotController {

    private final ChatbotConversationService conversationService;
    private final ChatbotMessageService messageService;
    private final ChatbotMessagePersistenceService persistenceService;
    private final ChatbotTicketEscalationService ticketEscalationService;

    @GetMapping("/home")
    public ResponseEntity<ChatbotHomeResponse> getHome() {
        return ResponseEntity.ok(new ChatbotHomeResponse(
                "Fitness Support",
                "Your personal assistant for app-related questions",
                "Hi! I'm your fitness app support assistant. How can I help you today?",
                List.of(
                        "I need help with my account",
                        "How do I use the BMI calculator?",
                        "I'm having trouble with my workout",
                        "How can I contact support?"
                )
        ));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ChatConversationResponse> createConversation(
            @RequestBody(required = false)
            @Valid CreateChatConversationRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createConversation(
                        authenticatedUserId(authentication), request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ChatPageResponse<ChatConversationResponse>> getConversations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {

        return ResponseEntity.ok(conversationService.getConversations(
                authenticatedUserId(authentication), page, size));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatConversationResponse> getConversation(
            @PathVariable UUID conversationId,
            Authentication authentication) {

        return ResponseEntity.ok(conversationService.getConversation(
                conversationId, authenticatedUserId(authentication)));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatPageResponse<ChatMessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(conversationService.getMessages(
                conversationId, authenticatedUserId(authentication), page, size));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<SendChatMessageResponse> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendChatMessageRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(messageService.sendMessage(
                conversationId, authenticatedUserId(authentication), request));
    }

    @PostMapping("/messages/{messageId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable UUID messageId,
            @Valid @RequestBody SubmitChatFeedbackRequest request,
            Authentication authentication) {

        persistenceService.submitFeedback(
                messageId,
                authenticatedUserId(authentication),
                request.rating(),
                request.feedbackText()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/{conversationId}/escalate")
    public ResponseEntity<ChatTicketResponse> escalateConversation(
            @PathVariable UUID conversationId,
            @Valid @RequestBody EscalateChatRequest request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketEscalationService.escalate(
                        conversationId,
                        authenticatedUserId(authentication),
                        request
                ));
    }

    @GetMapping("/conversations/{conversationId}/ticket")
    public ResponseEntity<ChatTicketResponse> getLinkedTicket(
            @PathVariable UUID conversationId,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketEscalationService.getLinkedTicket(
                        conversationId,
                        authenticatedUserId(authentication)
                )
        );
    }

    private String authenticatedUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication is required");
        }

        /*
         * IMPORTANT:
         * This assumes Authentication.getName() contains the Firebase UID
         * (the subject stored in your app JWT). If your JWT filter exposes
         * a custom principal, adapt this method to read that principal's UID.
         */
        return authentication.getName();
    }
}
