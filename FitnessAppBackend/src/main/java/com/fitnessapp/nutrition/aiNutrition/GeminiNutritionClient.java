package com.fitnessapp.nutrition.aiNutrition;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiNutritionClient {
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;


    @Value("${nutrition.ai.gemini.enabled:false}")
    private boolean enabled;
    @Value("${nutrition.ai.gemini.api-key:}")
    private String apiKey;
    @Value("${nutrition.ai.gemini.model:gemini-flash-latest}")
    private String model;
    @Value("${nutrition.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public JsonNode generateJson(String prompt) {
        log.debug("Generating JSON via Gemini model: {}", model);
        return callGemini(List.of(Map.of("text", prompt)));
    }

    public JsonNode analyzeImage(byte[] image, String mimeType, String notes) {
        log.debug("Analyzing food image via Gemini (mimeType={}, sizeBytes={})", mimeType, image != null ? image.length : 0);
        String data = Base64.getEncoder().encodeToString(image);
        String prompt = "Identify visible food ingredients only. Do not infer hidden ingredients. " +
                "Return ONLY JSON with this shape: {\"ingredients\":[{\"name\":\"...\",\"confidence\":0.0,\"estimatedQuantity\":\"...\"}],\"notes\":\"...\"}. " +
                "Confidence must be between 0 and 1. If uncertain, use lower confidence. User notes: " + safe(notes);
        return callGemini(List.of(Map.of("text", prompt), Map.of("inline_data", Map.of("mime_type", mimeType, "data", data))));
    }

    private JsonNode callGemini(List<Map<String, Object>> parts) {
        if (!enabled || !StringUtils.hasText(apiKey)) {
            log.warn("Gemini Nutrition AI request rejected: enabled={}, apiKeyPresent={}", enabled, StringUtils.hasText(apiKey));
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini Nutrition AI is not configured. Set NUTRITION_GEMINI_ENABLED=true and GEMINI_API_KEY.");
        }
        String url = baseUrl.replaceAll("/$", "") + "/models/" + model + ":generateContent";
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("role", "user", "parts", parts)),
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.2, "maxOutputTokens", 4096)
        );
        try {
            log.debug("Sending POST request to Gemini endpoint: {}", url);
            RestClient client = restClientBuilder.build();
            JsonNode response = client.post()
                    .uri(url)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode textNode = response == null ? null : response.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");
            if (textNode == null || !textNode.isTextual() || !StringUtils.hasText(textNode.asText())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned an empty response.");
            }
            return objectMapper.readTree(textNode.asText());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Gemini Nutrition AI HTTP request failed for URL: {}", url);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini Nutrition AI request failed.", ex);
        } catch (Exception ex) {
            log.error("Failed to parse JSON response from Gemini", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned invalid JSON.", ex);
        }
    }

    private String safe(String value) {
        if (value == null) return "None";
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }
}
