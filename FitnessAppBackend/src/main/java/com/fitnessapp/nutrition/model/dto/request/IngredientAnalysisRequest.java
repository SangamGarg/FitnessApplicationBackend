package com.fitnessapp.nutrition.model.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public record IngredientAnalysisRequest(
        @NotEmpty @Size(max = 60) List<@NotBlank @Size(max = 100) String> ingredients,
        @Size(max = 2048) String photoUrl,
        @Size(max = 1000) String notes
) {
}
