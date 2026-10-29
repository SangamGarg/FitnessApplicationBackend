package com.fitnessapp.support.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupportTicketMessageRequestDTO(
        @NotBlank @Size(max = 5000) String message
) {
}
