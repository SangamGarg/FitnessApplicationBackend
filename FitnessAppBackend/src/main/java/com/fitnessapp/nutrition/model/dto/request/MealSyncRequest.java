package com.fitnessapp.nutrition.model.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record MealSyncRequest(
    @NotEmpty @Size(max=100) List<@Valid LogMealRequest> meals
) {}
