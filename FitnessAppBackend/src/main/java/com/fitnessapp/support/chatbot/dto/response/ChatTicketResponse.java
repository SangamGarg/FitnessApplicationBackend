package com.fitnessapp.support.chatbot.dto.response;

import com.fitnessapp.support.models.dtos.response.SupportTicketResponseDTO;
import com.fitnessapp.support.models.enums.TicketPriority;
import com.fitnessapp.support.models.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatTicketResponse(
        UUID conversationId,
        String ticketNumber,
        TicketStatus status,
        TicketPriority priority,
        LocalDateTime createdAt
) {

    public static ChatTicketResponse from(
            UUID conversationId,
            SupportTicketResponseDTO ticket) {

        return new ChatTicketResponse(
                conversationId,
                ticket.getTicketNumber(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt()
        );
    }
}