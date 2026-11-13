
package com.fitnessapp.support.chatbot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class ChatbotConfig {

    @Bean
    public RestClient geminiRestClient(
            RestClient.Builder builder,
            GeminiProperties properties
    ) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("x-goog-api-key", properties.getApiKey())
                .build();
    }
}
