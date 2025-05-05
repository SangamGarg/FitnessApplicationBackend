package com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto;

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
    @Min(value = 0, message = "Age must be non-negative")
    @Max(value = 150, message = "Age must be realistic (<= 150)")
    @NotNull(message = "Age must not be Null")
    private Integer age;

    @NotBlank(message = "Gender must not be blank")
    @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be 'male' or 'female'")
    private String gender;

    @Positive(message = "Weight must be greater than 0")
    @Max(value = 300, message = "Weight must be at most 300kg")
    private Float weightKg;

    @Positive(message = "Height must be greater than 0")
    @Max(value = 300, message = "Height must be at most 300cm")
    private Float heightCm;

    @Positive(message = "Waist measurement must be greater than 0")
    private Float waist;

    @Positive(message = "Hip measurement must be greater than 0")
    private Float hip;

    @Positive(message = "Neck measurement must be greater than 0")
    private Float neck;

}