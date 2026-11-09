package com.fitnessapp.exercise.app.model.entity.entityNew;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
    name = "workout_plan_exercises",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_plan_day_exercise_order", columnNames = {"workout_plan_day_id", "sort_order"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class WorkoutPlanExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_day_id", nullable = false)
    private WorkoutPlanDay workoutPlanDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "target_sets", nullable = false)
    private short targetSets;

    @Column(name = "target_reps_min")
    private Integer targetRepsMin;

    @Column(name = "target_reps_max")
    private Integer targetRepsMax;

    @Column(name = "target_duration_seconds")
    private Integer targetDurationSeconds;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "target_weight_kg", precision = 7, scale = 2)
    private BigDecimal targetWeightKg;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
