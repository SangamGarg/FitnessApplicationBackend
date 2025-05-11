package com.fitnessapp.DeveloperAndTestingService.exerciseDevService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class DevExerciseCategoryRequestDto {

    @NotBlank(message = "Category name must not be blank")
    private String name; // Yoga, Strength, Warmup, Cardio, etc.

    @NotBlank(message = "Image URL must not be blank")
    @URL(message = "Invalid image URL format")
    private String imageUrl;

  //  @NotNull(message = "Levels list must not be null")
  //  @Size(min = 1, message = "At least one level must be provided")
    private List<DevExerciseLevelRequestDto> levels;

}