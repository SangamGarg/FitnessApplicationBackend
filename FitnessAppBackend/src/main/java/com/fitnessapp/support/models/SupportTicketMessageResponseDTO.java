package com.fitnessapp.support.models;

import java.time.LocalDateTime;

public record SupportTicketMessageResponseDTO(
        Long id,
        String actorEmail,
        String message,
        boolean internalNote,
        LocalDateTime createdAt
) {
}
