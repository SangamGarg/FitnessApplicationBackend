package com.fitnessapp.nutrition.model.dto.response;

public record DetectedIngredientResponse(String name, double confidence, String estimatedQuantity) {
}
