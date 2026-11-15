package com.fitnessapp.support.chatbot.controller;

import com.fitnessapp.support.chatbot.model.dto.request.CreateChatConversationRequest;
import com.fitnessapp.support.chatbot.model.dto.request.EscalateChatRequest;
import com.fitnessapp.support.chatbot.model.dto.request.SendChatMessageRequest;
import com.fitnessapp.support.chatbot.model.dto.request.SubmitChatFeedbackRequest;
import com.fitnessapp.support.chatbot.model.dto.response.*;
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
            @Valid CreateChatConversationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createConversation(
                        request));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ChatPageResponse<ChatConversationResponse>> getConversations(

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {

        return ResponseEntity.ok(conversationService.getConversations(
                page, size));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatConversationResponse> getConversation(
            @PathVariable UUID conversationId) {

        return ResponseEntity.ok(conversationService.getConversation(
                conversationId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatPageResponse<ChatMessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(conversationService.getMessages(
                conversationId, page, size));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<SendChatMessageResponse> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendChatMessageRequest request) {

        return ResponseEntity.ok(messageService.sendMessage(
                conversationId, request));
    }

    @PostMapping("/messages/{messageId}/feedback")
    public ResponseEntity<Void> submitFeedback(
            @PathVariable UUID messageId,
            @Valid @RequestBody SubmitChatFeedbackRequest request) {

        persistenceService.submitFeedback(
                messageId,

                request.rating(),
                request.feedbackText()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/{conversationId}/escalate")
    public ResponseEntity<ChatTicketResponse> escalateConversation(
            @PathVariable UUID conversationId,
            @Valid @RequestBody EscalateChatRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketEscalationService.escalate(
                        conversationId,

                        request
                ));
    }

    @GetMapping("/conversations/{conversationId}/ticket")
    public ResponseEntity<ChatTicketResponse> getLinkedTicket(
            @PathVariable UUID conversationId) {

        return ResponseEntity.ok(
                ticketEscalationService.getLinkedTicket(conversationId)
        );
    }
}
