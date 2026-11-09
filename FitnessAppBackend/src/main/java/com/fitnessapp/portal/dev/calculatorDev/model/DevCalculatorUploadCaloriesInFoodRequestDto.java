package com.fitnessapp.portal.dev.calculatorDev.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Request body for uploading (POST) or patching (PATCH) a calories-in-food item.
 * <p>
 * On <b>upload</b>, every field is mandatory (enforced in the service).
 * On <b>patch</b>, only {@code name} is mandatory; any other {@code null} field is left unchanged.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevCalculatorUploadCaloriesInFoodRequestDto {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @PositiveOrZero(message = "Calories must be zero or positive")
    private Double calories;

    @PositiveOrZero(message = "Serving size must be zero or positive")
    private Double servingSizeG;

    @PositiveOrZero(message = "Protein must be zero or positive")
    private Double proteinG;

    @PositiveOrZero(message = "Carbohydrates must be zero or positive")
    private Double carbohydratesTotalG;

    @PositiveOrZero(message = "Fiber must be zero or positive")
    private Double fiberG;

    @PositiveOrZero(message = "Saturated fat must be zero or positive")
    private Double fatSaturatedG;

    @PositiveOrZero(message = "Total fat must be zero or positive")
    private Double fatTotalG;

    @PositiveOrZero(message = "Cholesterol must be zero or positive")
    private Double cholesterolMg;

    @PositiveOrZero(message = "Sugar must be zero or positive")
    private Double sugarG;

    @PositiveOrZero(message = "Sodium must be zero or positive")
    private Double sodiumMg;

    @PositiveOrZero(message = "Potassium must be zero or positive")
    private Double potassiumMg;

    @URL(message = "Not a valid URL")
    private String imageUrl;
}
