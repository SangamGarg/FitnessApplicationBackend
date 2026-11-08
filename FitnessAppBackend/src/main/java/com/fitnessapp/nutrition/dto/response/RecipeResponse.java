package com.fitnessapp.nutrition.dto.response;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.RecipeDifficulty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record RecipeResponse(UUID id, String title, String description,
    Integer prepTimeMinutes, RecipeDifficulty difficulty, DietaryPreference dietType,
    Integer servings, BigDecimal calories, BigDecimal protein, BigDecimal carbohydrates,
    BigDecimal fat, BigDecimal fiber, BigDecimal sugar, String imageUrl,
    List<String> ingredients, List<String> instructions, boolean aiGenerated,
    String nutritionDataStatus, String nutritionDisclaimer) {}
