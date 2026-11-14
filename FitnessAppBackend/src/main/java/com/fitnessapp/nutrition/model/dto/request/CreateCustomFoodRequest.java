package com.fitnessapp.nutrition.model.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateCustomFoodRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 100) String brand,
        @NotBlank @Size(max = 80) String servingLabel,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal servingSizeGrams,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal calories,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal protein,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal carbohydrates,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal fat,
        @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal fiber,
        @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal sugar
) {
}
