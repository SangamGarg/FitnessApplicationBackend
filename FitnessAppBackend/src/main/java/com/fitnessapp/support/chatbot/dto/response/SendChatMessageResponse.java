package com.fitnessapp.support.chatbot.dto.response;

import com.fitnessapp.support.chatbot.knowledge.FaqReferenceResponse;

import java.util.List;
import java.util.UUID;

public record SendChatMessageResponse(
        UUID conversationId,
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage,
        List<FaqReferenceResponse> faqReferences,
        boolean humanSupportRecommended
) {
}