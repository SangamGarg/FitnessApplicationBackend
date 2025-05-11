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
    @Column(name = "exercise_detail_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    private String videoUrl;
    private String imageUrl;
    private Integer durationInSeconds;
    private Integer repetitions;
    private Integer sets;
    private String category;
    //    @ElementCollection(fetch = FetchType.LAZY)
    //    @Column(nullable = false, unique = true)
    //    @CollectionTable(name = "fcm_tokens", joinColumns = @JoinColumn(name = "user_details_id"))
    private List<String> muscleGroups; //=new ArrayList<>();
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