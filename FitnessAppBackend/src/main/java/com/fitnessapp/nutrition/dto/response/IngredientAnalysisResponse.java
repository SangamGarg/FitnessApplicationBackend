package com.fitnessapp.nutrition.dto.response;
import java.util.List;
public record IngredientAnalysisResponse(List<String> ingredients, String source,
                                         String message) {}
