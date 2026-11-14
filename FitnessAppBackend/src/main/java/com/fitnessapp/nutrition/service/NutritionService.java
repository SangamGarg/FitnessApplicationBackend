package com.fitnessapp.nutrition.service;

import com.fitnessapp.nutrition.model.dto.request.*;
import com.fitnessapp.nutrition.model.dto.response.*;
import com.fitnessapp.nutrition.model.enums.MealType;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface NutritionService {

    NutritionHomeResponse getNutritionHome();
    Page<FoodItemResponse> searchFoods(String query, int page, int size);

    MealEntryResponse logMeal(LogMealRequest request);

    MealSyncResponse sync(MealSyncRequest request);

    MealEntryResponse logMealSnapshot(String foodName, BigDecimal quantity,
                                      BigDecimal calories, BigDecimal protein, BigDecimal carbohydrates,
                                      BigDecimal fat, BigDecimal fiber, BigDecimal sugar,
                                      MealType mealType, LocalDate date, String notes, String photoUrl);

    MealEntryResponse updateMeal(UUID mealId, UpdateMealRequest request);

    void deleteMeal(UUID mealId);

    DailyNutritionSummaryResponse getDailySummary(LocalDate date);

    Page<MealEntryResponse> getMealHistory(int page, int size);

    NutritionGoalResponse getGoals();

    NutritionGoalResponse updateGoals(UpdateNutritionGoalRequest request);

    DietPreferencesResponse getPreferences();

    DietPreferencesResponse updatePreferences(UpdateDietPreferencesRequest request);

    Page<RecipeResponse> searchRecipes(String query, com.fitnessapp.nutrition.model.enums.DietaryPreference diet, int page, int size);

    RecipeResponse getRecipe(UUID recipeId);


    MealEntryResponse logRecipe(UUID recipeId, com.fitnessapp.nutrition.model.enums.MealType mealType, java.time.LocalDate date);

    IngredientAnalysisResponse normalizeIngredients(IngredientAnalysisRequest request);

    java.util.List<RecipeResponse> generateRecipes(AiRecipeRequest request);

    ImageIngredientAnalysisResponse analyzeIngredientImage(byte[] image, String mimeType, String notes);
}
