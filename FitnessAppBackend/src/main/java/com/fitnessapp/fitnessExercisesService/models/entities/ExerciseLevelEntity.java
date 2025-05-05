package com.fitnessapp.fitnessExercisesService.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exercise_level")
public class ExerciseLevelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_level_id")
    private Long id;

    private String level; // Beginner, Intermediate, Advanced

    @ManyToOne
    @JoinColumn(name = "exercise_category_id")
    @JsonBackReference
    private ExerciseCategoryEntity category;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ExerciseSubCategoryEntity> subCategories;
}
