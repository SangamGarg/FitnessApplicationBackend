package com.fitnessapp.support.chatbot.dto.request;

import com.fitnessapp.support.models.enums.SupportIssueCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EscalateChatRequest(

        @NotNull(message = "Category is required")
        SupportIssueCategory category,

        @NotBlank(message = "Subject is required")
        @Size(min = 5, max = 100, message = "Subject must be between 5 and 100 characters")
        String subject,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 1500, message = "Description must be between 10 and 1500 characters")
        String description

) {
}