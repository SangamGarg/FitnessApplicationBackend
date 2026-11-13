package com.fitnessapp.support.chatbot.knowledge;

public record RetrievedFaq(
        Long id,
        String categoryKey,
        String categoryName,
        String question,
        String answer,
        int relevanceScore
) {
}