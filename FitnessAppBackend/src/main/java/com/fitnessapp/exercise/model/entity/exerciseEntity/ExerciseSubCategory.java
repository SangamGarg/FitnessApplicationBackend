package com.fitnessapp.exercise.model.entity.exerciseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "exercise_sub_category",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_exercise_sub_category_key",
                columnNames = {"category_id", "sub_category_key"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ExerciseSubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ExerciseCategory category;

    @Column(name = "sub_category_key", nullable = false, length = 80)
    private String subCategoryKey;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
