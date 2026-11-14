package com.fitnessapp.nutrition.model.dto.request;

import com.fitnessapp.nutrition.model.enums.DietFitnessGoal;
import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import jakarta.validation.constraints.*;

import java.util.List;

public record AiRecipeRequest(
        @NotEmpty @Size(max = 40) List<@NotBlank @Size(max = 100) String> ingredients,
        DietaryPreference dietaryPreference,
        DietFitnessGoal fitnessGoal,
        @Min(1) @Max(240) Integer maxPrepTimeMinutes,
        @Min(1) @Max(10) Integer recipeCount
) {
}
