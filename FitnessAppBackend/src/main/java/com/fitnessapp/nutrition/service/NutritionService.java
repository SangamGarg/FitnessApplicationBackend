package com.fitnessapp.nutrition.service;

import com.fitnessapp.nutrition.dto.request.*;
import com.fitnessapp.nutrition.dto.response.*;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.util.UUID;

public interface NutritionService {
    Page<FoodItemResponse> searchFoods(String userId, String query, int page, int size);
    FoodItemResponse createCustomFood(String userId, CreateCustomFoodRequest request);
    MealEntryResponse logMeal(String userId, LogMealRequest request);
    MealEntryResponse logMealSnapshot(String userId, String foodName, java.math.BigDecimal quantity,
        java.math.BigDecimal calories, java.math.BigDecimal protein, java.math.BigDecimal carbohydrates,
        java.math.BigDecimal fat, java.math.BigDecimal fiber, java.math.BigDecimal sugar,
        com.fitnessapp.nutrition.model.enums.MealType mealType, LocalDate date, String notes, String photoUrl);
    MealEntryResponse updateMeal(String userId, UUID mealId, UpdateMealRequest request);
    void deleteMeal(String userId, UUID mealId);
    DailyNutritionSummaryResponse getDailySummary(String userId, LocalDate date);
    Page<MealEntryResponse> getMealHistory(String userId, int page, int size);
    NutritionGoalResponse getGoals(String userId);
    NutritionGoalResponse updateGoals(String userId, UpdateNutritionGoalRequest request);
}
