package com.fitnessapp.nutrition.model.dto.request;
import com.fitnessapp.nutrition.model.enums.MealType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public record UpdateMealRequest(
    @NotNull UUID foodId, @NotNull MealType mealType, @NotNull LocalDate mealDate,
    @NotNull @DecimalMin("0.01") @Digits(integer=8,fraction=2) BigDecimal quantityGrams,
    @Size(max=500) String notes, @Size(max=2048) String photoUrl
) {}
