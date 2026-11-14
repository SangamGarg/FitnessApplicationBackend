package com.fitnessapp.nutrition.model.dto.response;

import java.util.List;

public record ImageIngredientAnalysisResponse(List<DetectedIngredientResponse> ingredients, String source,
                                              String disclaimer) {
}
