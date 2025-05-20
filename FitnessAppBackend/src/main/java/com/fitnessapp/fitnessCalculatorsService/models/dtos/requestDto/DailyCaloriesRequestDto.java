package com.fitnessapp.fitnessCalculatorsService.models.dtos.requestDto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCaloriesRequestDto {
    @Min(value = 0, message = "Age must be a non-negative number")
    @Max(value = 150, message = "Age must be realistic (<= 150)")
    @NotNull(message = "Age must not be null")
    private Integer age;

    //^	Start of the string
    //(?i)	Case-insensitive flag (so "Male", "MALE", "male" are all accepted)
    //$	End of the string
    @NotBlank(message = "Gender must not be blank")
    @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be 'male' or 'female'")
    private String gender;

    @Positive(message = "Weight must be greater than 0")
    @Max(value = 300, message = "Weight must be at most 300kg")
    private Float weightKg;

    @Positive(message = "Height must be greater than 0")
    @Max(value = 300, message = "Height must be at most 300cm")
    private Float heightCm;

    @NotBlank(message = "Activity level must not be blank")
    @Pattern(
            regexp = "^(?i)(level_1|level_2|level_3|level_4|level_5)$",
            message = "Activity level must be one of: level_1, level_2, level_3, level_4, level_5"
    )
    private String activityLevel;
}
