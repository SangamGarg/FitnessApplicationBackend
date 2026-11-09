package com.fitnessapp.nutrition.dto.response;

public record DetectedIngredientResponse(String name, double confidence, String estimatedQuantity) {}
