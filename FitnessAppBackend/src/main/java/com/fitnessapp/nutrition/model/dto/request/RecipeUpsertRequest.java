package com.fitnessapp.nutrition.model.dto.request;

import com.fitnessapp.nutrition.model.enums.DietaryPreference;
import com.fitnessapp.nutrition.model.enums.RecipeDifficulty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RecipeUpsertRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 1000) String description,
        @NotNull @Min(1) @Max(600) Integer prepTimeMinutes,
        @NotNull RecipeDifficulty difficulty,
        @NotNull DietaryPreference dietType,
        @NotNull @Min(1) @Max(30) Integer servings,
        @NotNull @DecimalMin("0") BigDecimal calories,
        @NotNull @DecimalMin("0") BigDecimal protein,
        @NotNull @DecimalMin("0") BigDecimal carbohydrates,
        @NotNull @DecimalMin("0") BigDecimal fat,
        @DecimalMin("0") BigDecimal fiber, @DecimalMin("0") BigDecimal sugar,
        @Size(max = 2048) String imageUrl,
        @NotEmpty @Size(max = 40) List<@NotBlank @Size(max = 240) String> ingredients,
        @NotEmpty @Size(max = 40) List<@NotBlank @Size(max = 1000) String> instructions
) {
}
