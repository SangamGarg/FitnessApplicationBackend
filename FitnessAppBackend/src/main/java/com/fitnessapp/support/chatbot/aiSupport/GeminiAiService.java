package com.fitnessapp.support.chatbot.aiSupport;

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
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new GeminiApiException("AI support is not configured");
        }

        Map<String, Object> payload = Map.of(
                "model", properties.getModel(),
                "input", request.input(),
                "system_instruction", request.systemInstruction(),
                "store", false,
                "generation_config", Map.of(
                        "temperature", 0.3,
                        "max_output_tokens", 600
                )
        );

        try {
            JsonNode response = geminiRestClient.post()
                    .uri("/interactions")
                    .header("x-goog-api-key", properties.getApiKey())
                    .body(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (httpRequest, httpResponse) -> {
                        log.error("Gemini API returned HTTP status {}", httpResponse.getStatusCode());
                        throw new GeminiApiException("AI support is temporarily unavailable");
                    })
                    .body(JsonNode.class);

            if (response == null) {
                throw new GeminiApiException("AI service returned an empty response");
            }

            String status = response.path("status").asText("");
            if (!status.isBlank() && !"completed".equalsIgnoreCase(status)) {
                log.warn("Gemini interaction did not complete. Status: {}", status);
                throw new GeminiApiException("AI service could not complete the response");
            }

            String answer = extractAnswer(response);
            if (answer.isBlank()) {
                log.warn("Gemini returned no text output");
                throw new GeminiApiException("AI service returned an empty response");
            }

            return new ChatAiResponse(answer);
        } catch (GeminiApiException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("Gemini HTTP error. Status: {}", ex.getStatusCode());
            throw new GeminiApiException("AI support is temporarily unavailable");
        } catch (RestClientException ex) {
            log.error("Failed to communicate with Gemini API", ex);
            throw new GeminiApiException("Unable to connect to AI service");
        }
    }

    private String extractAnswer(JsonNode response) {
        JsonNode steps = response.path("steps");
        StringBuilder answer = new StringBuilder();

        if (steps.isArray()) {
            for (JsonNode step : steps) {
                if (!"model_output".equals(step.path("type").asText())) {
                    continue;
                }

                JsonNode content = step.path("content");
                if (!content.isArray()) {
                    continue;
                }

                for (JsonNode item : content) {
                    if ("text".equals(item.path("type").asText())) {
                        String text = item.path("text").asText("");
                        if (!text.isBlank()) {
                            if (!answer.isEmpty()) {
                                answer.append('\n');
                            }
                            answer.append(text);
                        }
                    }
                }
            }
        }

        return answer.toString().trim();
    }
}
