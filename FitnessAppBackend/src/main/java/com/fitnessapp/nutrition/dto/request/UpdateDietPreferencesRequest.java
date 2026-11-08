package com.fitnessapp.nutrition.dto.request;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.DietFitnessGoal;
import jakarta.validation.constraints.Size;
public record UpdateDietPreferencesRequest(
    DietaryPreference dietaryPreference, DietFitnessGoal fitnessGoal,
    @Size(max=1000) String allergies, @Size(max=1500) String excludedIngredients
) {}
