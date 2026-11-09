package com.fitnessapp.nutrition.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FoodSearchRequest(
        @Size(max = 100) String query,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
) {}
