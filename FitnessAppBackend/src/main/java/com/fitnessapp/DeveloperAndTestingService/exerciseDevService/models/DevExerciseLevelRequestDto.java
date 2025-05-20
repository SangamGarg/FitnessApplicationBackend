package com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevExerciseLevelRequestDto {

    @NotBlank(message = "Level must not be blank")
    private String level; // Beginner, Intermediate, Advanced

  //  @NotNull(message = "Subcategories list must not be null")
  //  @Size(min = 1, message = "At least one subcategory must be provided")
    private List<DevExerciseSubCategoryRequestDto> subCategories;
}
