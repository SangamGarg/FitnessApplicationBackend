package com.fitnessapp.nutrition.model.dto.request;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import jakarta.validation.constraints.*;
public record RecipeSearchRequest(@Size(max=100) String query,
    DietaryPreference dietaryPreference, @Min(0) Integer page,
    @Min(1) @Max(100) Integer size) {}
