package com.fitnessapp.support.chatbot.dto.response;

import java.util.List;

public record ChatbotHomeResponse(
        String title,
        String subtitle,
        String welcomeMessage,
        List<String> suggestedQuestions
) {
}
