package com.fitnessapp.support.chatbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fitness.chatbot.gemini")
public class GeminiProperties {

    private String apiKey;

    private String model = "gemini-3.8-flash";

    private String baseUrl =
            "https://generativelanguage.googleapis.com/v1beta";
}
