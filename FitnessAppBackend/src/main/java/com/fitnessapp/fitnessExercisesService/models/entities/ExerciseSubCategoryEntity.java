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
@Table(name = "exercise_sub_category")
public class ExerciseSubCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_subCategory_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., Arms, Chest

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "exercise_level_id")
    @JsonBackReference
    private ExerciseLevelEntity level;

    @OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ExerciseDetailEntity> exercises;
}
