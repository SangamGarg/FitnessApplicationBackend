package com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevExerciseSubCategoryRequestDto {
    @NotBlank(message = "Name must not be blank")
    private String name; // like for strength beginner - Arms, legs // for cardio intermediate - etc etc

    @NotBlank(message = "Image URL must not be blank")
    @URL(message = "Invalid image URL format")
    private String imageUrl;

    //   @NotNull(message = "Exercises list must not be null")
    // @Size(min = 1, message = "At least one exercise must be provided")
    private List<DevExerciseDetailsRequestDto> exercises;
}
