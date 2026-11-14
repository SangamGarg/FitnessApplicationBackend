package com.fitnessapp.nutrition.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyNutritionSummaryResponse(
        LocalDate date,
        NutritionGoalResponse target,
        NutrientTotals consumed,
        NutrientTotals remaining,
        BigDecimal calorieProgressPercent,
        List<MealEntryResponse> meals
) {
    public record NutrientTotals(
            BigDecimal calories, BigDecimal protein,
            BigDecimal carbohydrates, BigDecimal fat,
            BigDecimal fiber, BigDecimal sugar
    ) {}
}
