package com.fitnessapp.appAuth.model.dtos;

import com.fitnessapp.appAuth.model.enums.ActivityLevel;
import com.fitnessapp.appAuth.model.enums.FitnessGoal;
import com.fitnessapp.appAuth.model.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String email;

    @NotNull(message = "Gender cannot be null")
    private Gender gender;

    @NotNull(message = "Age cannot be null")
    @Min(value = 13, message = "Minimum age must be 13")
    @Max(value = 120, message = "Age cannot exceed 120")
    private Integer age;

    @Pattern(regexp = "^(https?)://.+$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Profile image URL must start with http:// or https://")
    @Size(max = 2048, message = "Profile image URL cannot exceed 2048 characters")
    private String profileImageUrl;

    @NotNull(message = "Height cannot be null")
    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250 cm")
    private BigDecimal heightCm;

    @NotNull(message = "Weight cannot be null")
    @DecimalMin(value = "10.0", message = "Weight must be at least 10 kg")
    @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
    private BigDecimal weightKg;

    @NotNull(message = "Activity level cannot be null")
    private ActivityLevel activityLevel;

    @NotNull(message = "Goal cannot be null")
    private FitnessGoal goal;
}
