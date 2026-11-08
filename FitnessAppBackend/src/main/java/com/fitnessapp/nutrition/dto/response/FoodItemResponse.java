package com.fitnessapp.nutrition.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FoodItemResponse(
        UUID id, String name, String brand, String servingLabel,
        BigDecimal servingSizeGrams, BigDecimal calories, BigDecimal protein,
        BigDecimal carbohydrates, BigDecimal fat, BigDecimal fiber,
        BigDecimal sugar, boolean verified, boolean custom
) {}
