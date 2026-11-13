
package com.fitnessapp.support.chatbot.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitnessapp.support.chatbot.config.GeminiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAiService implements ChatAiService {

    private final RestClient geminiRestClient;
    private final GeminiProperties properties;

    @Override
    public ChatAiResponse generateResponse(ChatAiRequest request) {

        if (properties.getApiKey() == null
                || properties.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is not configured"
            );
        }

        Map<String, Object> payload = Map.of(
                "model", properties.getModel(),
                "input", request.input(),
                "system_instruction", request.systemInstruction(),
                "store", false
        );

        try {
            JsonNode response = geminiRestClient
                    .post()
                    .uri("/interactions")
                    .body(payload)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (httpRequest, httpResponse) -> {
                                log.error(
                                        "Gemini API returned HTTP status: {}",
                                        httpResponse.getStatusCode()
                                );

                                throw new GeminiApiException(
                                        "AI service is temporarily unavailable"
                                );
                            }
                    )
                    .body(JsonNode.class);

            if (response == null) {
                throw new GeminiApiException(
                        "Empty response received from AI service"
                );
            }

            String answer = response.path("output_text").asText("");

            if (answer.isBlank()) {
                log.warn("Gemini returned an empty text response");
                throw new GeminiApiException(
                        "AI service returned an empty response"
                );
            }

            return new ChatAiResponse(answer.trim());

        } catch (RestClientResponseException ex) {
            log.error(
                    "Gemini HTTP error. Status: {}",
                    ex.getStatusCode()
            );

            throw new GeminiApiException(
                    "AI service is temporarily unavailable"
            );

        } catch (RestClientException ex) {
            log.error("Failed to communicate with Gemini API", ex);

            throw new GeminiApiException(
                    "Unable to connect to AI service"
            );
        }
    }
}
