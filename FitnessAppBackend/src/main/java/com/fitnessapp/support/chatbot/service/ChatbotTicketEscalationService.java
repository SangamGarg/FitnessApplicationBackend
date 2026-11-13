package com.fitnessapp.support.chatbot.service;

import com.fitnessapp.support.chatbot.dto.request.EscalateChatRequest;
import com.fitnessapp.support.chatbot.dto.response.ChatTicketResponse;
import com.fitnessapp.support.chatbot.entity.ChatConversationEntity;
import com.fitnessapp.support.chatbot.repository.ChatConversationRepository;
import com.fitnessapp.support.models.dtos.request.CreateSupportTicketRequestDTO;
import com.fitnessapp.support.models.dtos.response.SupportTicketResponseDTO;
import com.fitnessapp.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotTicketEscalationService {

    private static final int MAX_TICKET_DESCRIPTION_LENGTH = 2000;

    private final ChatConversationRepository conversationRepository;
    private final ChatbotMessagePersistenceService persistenceService;
    private final SupportTicketService supportTicketService;

    @Transactional
    public ChatTicketResponse escalate(
            UUID conversationId,
            String userId,
            EscalateChatRequest request) {

        ChatConversationEntity conversation = conversationRepository
                .findByIdAndUserIdForUpdate(conversationId, userId)
                .orElseThrow(ChatConversationNotFoundException::new);

        // Prevent duplicate ticket creation for this conversation.
        if (conversation.getLinkedTicketNumber() != null
                && !conversation.getLinkedTicketNumber().isBlank()) {

            SupportTicketResponseDTO existingTicket =
                    supportTicketService.getTicketByNumber(
                            conversation.getLinkedTicketNumber()
                    );

            return ChatTicketResponse.from(
                    conversationId,
                    existingTicket
            );
        }

        String recentContext = persistenceService.loadRecentContext(
                conversationId,
                userId
        );

        String ticketDescription = buildTicketDescription(
                request.description(),
                recentContext
        );

        CreateSupportTicketRequestDTO ticketRequest =
                new CreateSupportTicketRequestDTO();

        ticketRequest.setCategory(request.category());
        ticketRequest.setSubject(request.subject().trim());
        ticketRequest.setDescription(ticketDescription);

        SupportTicketResponseDTO createdTicket =
                supportTicketService.createTicket(
                        ticketRequest,
                        List.of()
                );

        conversation.setLinkedTicketNumber(
                createdTicket.getTicketNumber()
        );

        conversationRepository.save(conversation);

        return ChatTicketResponse.from(
                conversationId,
                createdTicket
        );
    }

    @Transactional(readOnly = true)
    public ChatTicketResponse getLinkedTicket(
            UUID conversationId,
            String userId) {

        ChatConversationEntity conversation = conversationRepository
                .findByIdAndUserId(conversationId, userId)
                .orElseThrow(ChatConversationNotFoundException::new);

        String ticketNumber = conversation.getLinkedTicketNumber();

        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No support ticket is linked to this conversation"
            );
        }

        SupportTicketResponseDTO ticket =
                supportTicketService.getTicketByNumber(ticketNumber);

        return ChatTicketResponse.from(conversationId, ticket);
    }

    private String buildTicketDescription(
            String userDescription,
            String recentContext) {

        String description = """
                User's issue description:
                %s

                Recent chatbot conversation:
                %s
                """.formatted(
                userDescription.trim(),
                recentContext == null ? "" : recentContext.trim()
        ).trim();

        if (description.length() > MAX_TICKET_DESCRIPTION_LENGTH) {
            description = description.substring(
                    0,
                    MAX_TICKET_DESCRIPTION_LENGTH
            );
        }

        return description;
    }
}