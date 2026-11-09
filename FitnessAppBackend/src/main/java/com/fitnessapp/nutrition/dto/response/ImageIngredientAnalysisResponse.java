package com.fitnessapp.nutrition.dto.response;

import java.util.List;

public record ImageIngredientAnalysisResponse(List<DetectedIngredientResponse> ingredients, String source, String disclaimer) {}
