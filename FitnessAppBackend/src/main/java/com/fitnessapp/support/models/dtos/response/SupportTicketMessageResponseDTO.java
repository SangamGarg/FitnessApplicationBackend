package com.fitnessapp.support.models.dtos.response;

import java.time.LocalDateTime;

public record SupportTicketMessageResponseDTO(
        Long id,
        String actorEmail,
        String message,
        boolean internalNote,
        LocalDateTime createdAt
) {
}
