package com.fitnessapp.nutrition.service;
import com.fitnessapp.nutrition.dto.request.*;
import com.fitnessapp.nutrition.dto.response.*;
import org.springframework.data.domain.Page;
import java.util.UUID;
public interface NutritionAdvancedService {
    DietPreferencesResponse getPreferences(String userId);
    DietPreferencesResponse updatePreferences(String userId, UpdateDietPreferencesRequest request);
    Page<RecipeResponse> searchRecipes(String userId, String query, com.fitnessapp.nutrition.model.enums.DietaryPreference diet, int page, int size);
    RecipeResponse getRecipe(String userId, UUID recipeId);
    RecipeResponse createRecipe(RecipeUpsertRequest request);
    MealEntryResponse logRecipe(String userId, UUID recipeId, com.fitnessapp.nutrition.model.enums.MealType mealType, java.time.LocalDate date);
    IngredientAnalysisResponse normalizeIngredients(IngredientAnalysisRequest request);
    java.util.List<RecipeResponse> generateRecipes(String userId, AiRecipeRequest request);
    ImageIngredientAnalysisResponse analyzeIngredientImage(byte[] image, String mimeType, String notes);
}
