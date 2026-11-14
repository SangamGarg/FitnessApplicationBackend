package com.fitnessapp.nutrition.model.dto.response;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.DietFitnessGoal;
public record DietPreferencesResponse(DietaryPreference dietaryPreference,
    DietFitnessGoal fitnessGoal, String allergies, String excludedIngredients) {}
