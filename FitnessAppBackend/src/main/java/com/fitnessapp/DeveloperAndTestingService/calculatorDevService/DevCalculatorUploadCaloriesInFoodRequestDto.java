package com.fitnessapp.DeveloperAndTestingService.calculatorDevService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevCalculatorUploadCaloriesInFoodRequestDto {
    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotNull(message = "Calories must not be null")
    @PositiveOrZero(message = "Calories must be zero or positive")
    private Double calories;

    @NotNull(message = "Serving size must not be null")
    @PositiveOrZero(message = "Serving size must be zero or positive")
    private Double servingSizeG;

    @NotNull(message = "Protein must not be null")
    @PositiveOrZero(message = "Protein must be zero or positive")
    private Double proteinG;

    @NotNull(message = "Carbohydrates must not be null")
    @PositiveOrZero(message = "Carbohydrates must be zero or positive")
    private Double carbohydratesTotalG;

    @NotNull(message = "Fiber must not be null")
    @PositiveOrZero(message = "Fiber must be zero or positive")
    private Double fiberG;

    @NotNull(message = "Saturated fat must not be null")
    @PositiveOrZero(message = "Saturated fat must be zero or positive")
    private Double fatSaturatedG;

    @NotNull(message = "Total fat must not be null")
    @PositiveOrZero(message = "Total fat must be zero or positive")
    private Double fatTotalG;

    @NotNull(message = "Cholesterol must not be null")
    @PositiveOrZero(message = "Cholesterol must be zero or positive")
    private Double cholesterolMg;

    @NotNull(message = "Sugar must not be null")
    @PositiveOrZero(message = "Sugar must be zero or positive")
    private Double sugarG;

    @NotNull(message = "Sodium must not be null")
    @PositiveOrZero(message = "Sodium must be zero or positive")
    private Double sodiumMg;

    @NotNull(message = "Potassium must not be null")
    @PositiveOrZero(message = "Potassium must be zero or positive")
    private Double potassiumMg;

    @NotBlank(message = "Image URL must not be blank")
    @URL(message = "Not Valid Url")
    private String imageUrl;
}
