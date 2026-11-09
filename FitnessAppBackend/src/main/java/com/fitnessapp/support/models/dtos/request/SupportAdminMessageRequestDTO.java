package com.fitnessapp.support.models.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupportAdminMessageRequestDTO(
        @NotBlank @Size(max = 5000) String message,
        boolean internalNote
) {
}
