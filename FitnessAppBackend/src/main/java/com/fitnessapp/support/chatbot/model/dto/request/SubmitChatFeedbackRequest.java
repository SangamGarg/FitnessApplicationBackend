package com.fitnessapp.support.chatbot.model.dto.request;

import com.fitnessapp.support.chatbot.model.enums.ChatFeedbackRating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitChatFeedbackRequest(
        @NotNull(message = "Feedback rating is required")
        ChatFeedbackRating rating,

        @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
        String feedbackText
) {
}
