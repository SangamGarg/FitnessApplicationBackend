
package com.fitnessapp.support.chatbot.aiSupport;

import lombok.Builder;

@Builder
public record ChatAiRequest(
        String systemInstruction,
        String input
) {
}
