package com.fitnessapp.support.chatbot.knowledge;

public record FaqReferenceResponse(
        Long faqId,
        String categoryKey,
        String categoryName,
        String question
) {

    public static FaqReferenceResponse from(RetrievedFaq faq) {
        return new FaqReferenceResponse(
                faq.id(),
                faq.categoryKey(),
                faq.categoryName(),
                faq.question()
        );
    }
}