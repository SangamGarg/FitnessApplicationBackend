package com.fitnessapp.nutrition.service;

import com.fitnessapp.nutrition.model.dto.request.AiRecipeRequest;
import com.fitnessapp.nutrition.model.dto.request.CreateCustomFoodRequest;
import com.fitnessapp.nutrition.model.dto.request.RecipeUpsertRequest;
import com.fitnessapp.nutrition.model.dto.response.FoodItemResponse;
import com.fitnessapp.nutrition.model.dto.response.RecipeResponse;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PortalNutritionService {
    Page<FoodItemResponse> searchFoods(String query, int page, int size);

    Page<RecipeResponse> searchRecipes(String query, DietaryPreference dietaryPreference, int page, int size);

    RecipeResponse getRecipe(UUID recipeId);

    List<RecipeResponse> generateRecipes(AiRecipeRequest request);

    FoodItemResponse createCustomFood(CreateCustomFoodRequest request);

    RecipeResponse createRecipe(RecipeUpsertRequest r);
}
