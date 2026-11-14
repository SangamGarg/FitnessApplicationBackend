package com.fitnessapp.nutrition.model.dto.response;
import java.util.List;
public record IngredientAnalysisResponse(List<String> ingredients, String source,
                                         String message) {}
