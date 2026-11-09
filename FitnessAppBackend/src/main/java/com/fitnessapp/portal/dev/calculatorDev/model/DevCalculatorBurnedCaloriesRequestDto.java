package com.fitnessapp.portal.dev.calculatorDev.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Request body for uploading (POST) or patching (PATCH) a burned-calories activity.
 * <p>
 * On <b>upload</b>, {@code metValue} and {@code imageUrl} are mandatory (enforced in the service).
 * On <b>patch</b>, only {@code name} is mandatory; any other {@code null} field is left unchanged.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevCalculatorBurnedCaloriesRequestDto {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @Positive(message = "MET value must be greater than zero")
    @DecimalMax(value = "30.0", message = "MET value must be less than or equal to 30")
    private Double metValue;

    @URL(message = "Not a valid URL")
    private String imageUrl;
}
