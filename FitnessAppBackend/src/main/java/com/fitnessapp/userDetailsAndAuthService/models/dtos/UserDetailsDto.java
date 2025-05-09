package com.fitnessapp.userDetailsAndAuthService.models.dtos;

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
public class UserDetailsDto {

    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String email;

    @NotNull(message = "FCM token list cannot be null")
    @NotEmpty(message = "FCM token list cannot be empty")
    private List<@NotBlank(message = "Each FCM token must be a non-blank string") String> firebaseCloudMessagingToken;


    //^	Start of the string
    //(?i)	Case-insensitive flag (so "Male", "MALE", "male" are all accepted)
    //$	End of the string
    @NotBlank(message = "Gender cannot be empty")
    @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be 'male' or 'female'")
    private String gender;

    @NotNull(message = "Age cannot be null")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be realistic (<=120)")
    private Integer age;

    @NotBlank(message = "Profile image URL cannot be empty")
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Profile image URL must be a valid URL"
    )
    private String profileImageUrl;

    @Positive(message = "Height must be positive")
    @Max(value = 300, message = "Height must be at most 300cm")
    private float heightCm;

    @Positive(message = "Weight must be positive")
    @Max(value = 300, message = "Weight must be at most 300kg")
    private float weightKg;

    @NotBlank(message = "Activity level cannot be empty")
    private String activityLevel;

    @NotBlank(message = "Goal cannot be empty")
    private String goal;

    @Positive(message = "Hip measurement must be positive")
    private float hip;

    @Positive(message = "Neck measurement must be positive")
    private float neck;

    @Positive(message = "Waist measurement must be positive")
    private float waist;
}
