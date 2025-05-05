package com.fitnessapp.fitnessExercisesService.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exercise_detail")
public class ExerciseDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String videoUrl;
    private String imageUrl;
    private Integer durationInSeconds;
    private Integer repetitions;
    private Integer sets;
    private String category;
    private List<String> muscleGroups;
    private String equipment;
    private String difficulty;
    private List<String> benefits;
    private List<String> instructions;
    private List<String> tips;
    private Double caloriesBurned;

    @ManyToOne
    @JoinColumn(name = "exercise_subCategory_id")
    @JsonBackReference
    private ExerciseSubCategoryEntity subCategory;
}
