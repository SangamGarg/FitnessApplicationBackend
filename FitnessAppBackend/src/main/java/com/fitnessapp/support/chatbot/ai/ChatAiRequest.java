
package com.fitnessapp.support.chatbot.ai;

import lombok.Builder;

@Builder
public record ChatAiRequest(
        String systemInstruction,
        String input
) {
}
