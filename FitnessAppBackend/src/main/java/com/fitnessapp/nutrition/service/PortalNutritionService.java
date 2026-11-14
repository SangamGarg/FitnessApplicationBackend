package com.fitnessapp.nutrition.service;

import com.fitnessapp.nutrition.model.dto.request.CreateCustomFoodRequest;
import com.fitnessapp.nutrition.model.dto.request.RecipeUpsertRequest;
import com.fitnessapp.nutrition.model.dto.response.FoodItemResponse;
import com.fitnessapp.nutrition.model.dto.response.RecipeResponse;

public interface PortalNutritionService {
    FoodItemResponse createCustomFood(CreateCustomFoodRequest request);

    RecipeResponse createRecipe(RecipeUpsertRequest r);
}
