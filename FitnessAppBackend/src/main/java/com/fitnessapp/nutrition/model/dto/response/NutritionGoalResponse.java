package com.fitnessapp.nutrition.model.dto.response;

import java.math.BigDecimal;

public record NutritionGoalResponse(
        BigDecimal dailyCalories, BigDecimal dailyProtein,
        BigDecimal dailyCarbohydrates, BigDecimal dailyFat
) {}
