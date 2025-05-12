package com.fitnessapp.userDetailsAndAuthService.models.dtos.requestDtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPatchRequestDto {

    private String name;

    private List<String> firebaseCloudMessagingToken;


    //^	Start of the string
    //(?i)	Case-insensitive flag (so "Male", "MALE", "male" are all accepted)
    //$	End of the string
    @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be 'male' or 'female'")
    private String gender;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be realistic (<=120)")
    private Integer age;

    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Profile image URL must be a valid URL"
    )
    private String profileImageUrl;

    @PositiveOrZero(message = "Height must be positive")
    @Max(value = 300, message = "Height must be at most 300cm")
    private float heightCm;

    @PositiveOrZero(message = "Weight must be positive")
    @Max(value = 300, message = "Weight must be at most 300kg")
    private float weightKg;

    @Pattern(
            regexp = "^(?i)(level_1|level_2|level_3|level_4|level_5)$",
            message = "Activity level must be one of: level_1, level_2, level_3, level_4, level_5"
    )
    private String activityLevel;

    @Pattern(
            regexp = "^(?i)(Maintain weight|Mild weight loss|Weight loss|Extreme weight loss|Mild weight gain|Weight gain|Extreme weight gain)$",
            message = "Goal must be one of: Maintain weight, Mild weight loss, Weight loss, Extreme weight loss, Mild weight gain, Weight gain, Extreme weight gain"
    )
    private String goal;

    @PositiveOrZero(message = "Hip measurement must be positive")
    @Max(value = 300, message = "Hip must be at most 300cm")
    private float hipCm;

    @PositiveOrZero(message = "Neck measurement must be positive")
    @Max(value = 100, message = "Neck must be at most 100cm")
    private float neckCm;

    @PositiveOrZero(message = "Waist measurement must be positive")
    @Max(value = 150, message = "Waist must be at most 150 cm")
    private float waistCm;
}
