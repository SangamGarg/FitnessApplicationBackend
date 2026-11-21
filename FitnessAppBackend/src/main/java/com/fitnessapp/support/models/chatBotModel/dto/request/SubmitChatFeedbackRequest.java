package com.fitnessapp.support.models.chatBotModel.dto.request;

import com.fitnessapp.support.models.chatBotModel.enums.ChatFeedbackRating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitChatFeedbackRequest(
        @NotNull(message = "Feedback rating is required")
        ChatFeedbackRating rating,

        @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
        String feedbackText
) {
}
