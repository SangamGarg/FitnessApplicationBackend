package com.fitnessapp.nutrition.dto.response;
import com.fitnessapp.nutrition.model.enums.MealType;
import com.fitnessapp.nutrition.model.enums.MealSyncStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
public record MealEntryResponse(
    UUID id, UUID foodId, String foodName, MealType mealType, LocalDate mealDate,
    BigDecimal quantityGrams, String quantityUnit, BigDecimal calories, BigDecimal protein,
    BigDecimal carbohydrates, BigDecimal fat, BigDecimal fiber, BigDecimal sugar,
    String notes, Instant createdAt, UUID clientMealId, String photoUrl,
    MealSyncStatus syncStatus
) {}
