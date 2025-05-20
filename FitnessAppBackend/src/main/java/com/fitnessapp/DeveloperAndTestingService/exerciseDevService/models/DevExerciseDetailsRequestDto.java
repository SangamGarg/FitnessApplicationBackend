package com.fitnessapp.DeveloperAndTestingService.exerciseDevService.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class DevExerciseDetailsRequestDto {
    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotBlank(message = "Video URL must not be blank")
    @URL(message = "Invalid video URL format")
    private String videoUrl;

    @NotBlank(message = "Image URL must not be blank")
    @URL(message = "Invalid image URL format")
    private String imageUrl;

    @Positive(message = "Duration must be greater than 0")
    @NotNull(message = "Duration In Seconds must not be null")
    private Integer durationInSeconds;

    @Positive(message = "Repetitions must be greater than 0")
    @NotNull(message = "Repetitions must not be null")
    private Integer repetitions;

    @Positive(message = "Sets must be greater than 0")
    @NotNull(message = "Sets must not be null")
    private Integer sets;

    @NotBlank(message = "Category must not be blank")
    private String category;

    @NotNull(message = "Muscle groups must not be null")
    @Size(min = 1, message = "At least one muscle group must be selected")
    private List<String> muscleGroups;

    @NotBlank(message = "Equipment must not be blank")
    private String equipment;

    @NotBlank(message = "Difficulty level must not be blank")
    private String difficulty;

    @NotNull(message = "Benefits must not be null")
    @Size(min = 1, message = "At least one benefit is required")
    private List<String> benefits;

    @NotNull(message = "Instructions must not be null")
    @Size(min = 1, message = "At least one instruction is required")
    private List<String> instructions;

    @NotNull(message = "Tips must not be null")
    @Size(min = 1, message = "At least one tip is required")
    private List<String> tips;

    @Positive(message = "Calories burned must be greater than 0")
    private Double caloriesBurned;
}
