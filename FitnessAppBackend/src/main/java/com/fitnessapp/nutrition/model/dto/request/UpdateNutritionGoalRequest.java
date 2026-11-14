package com.fitnessapp.nutrition.model.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateNutritionGoalRequest(
        @NotNull @DecimalMin("1") @Digits(integer = 8, fraction = 2) BigDecimal dailyCalories,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal dailyProtein,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal dailyCarbohydrates,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal dailyFat
) {}
