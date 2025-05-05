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

    private List< String> firebaseCloudMessagingToken;


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

    @Positive(message = "Height must be positive")
    @Max(value = 300, message = "Height must be at most 300cm")
    private float heightCm;

    @Positive(message = "Weight must be positive")
    @Max(value = 300, message = "Weight must be at most 300kg")
    private float weightKg;

    private String activityLevel;

    private String goal;

    @Positive(message = "Hip measurement must be positive")
    private float hip;

    @Positive(message = "Neck measurement must be positive")
    private float neck;

    @Positive(message = "Waist measurement must be positive")
    private float waist;
}
