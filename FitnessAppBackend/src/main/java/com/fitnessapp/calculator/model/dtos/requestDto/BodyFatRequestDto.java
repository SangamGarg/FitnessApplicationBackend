package com.fitnessapp.calculator.model.dtos.requestDto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BodyFatRequestDto {
    @Min(value = 13, message = "Age must be at least 13 years")
    @Max(value = 150, message = "Age must be realistic (<= 150)")
    @NotNull(message = "Age must not be Null")
    private Integer age;

    @NotBlank(message = "Gender must not be blank")
    @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be 'male' or 'female'")
    private String gender;

    @Positive(message = "Weight must be greater than 0")
    @Max(value = 300, message = "Weight must be at most 300kg")
    @NotNull
    private Float weightKg;

    @Positive(message = "Height must be greater than 0")
    @Max(value = 300, message = "Height must be at most 300cm")
    @Min(value = 120, message = "Height must be at least 120cm for this formula")
    @NotNull
    private Float heightCm;

    @Positive(message = "Waist measurement must be greater than 0")
    @NotNull
    private Float waistCm;

    @Positive(message = "Hip measurement must be greater than 0")
    @NotNull
    private Float hipCm;

    @Positive(message = "Neck measurement must be greater than 0")
    @NotNull
    private Float neckCm;

    @AssertTrue(message = "Waist and hip measurements must be anatomically consistent with neck measurement")
    public boolean isMeasurementGeometryValid() {
        if (gender == null || waistCm == null || hipCm == null || neckCm == null) {
            return true;
        }
        return gender.equalsIgnoreCase("male")
                ? waistCm > neckCm
                : waistCm + hipCm > neckCm;
    }
}