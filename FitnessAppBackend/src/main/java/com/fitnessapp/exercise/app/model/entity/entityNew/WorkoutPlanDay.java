package com.fitnessapp.exercise.app.model.entity.entityNew;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "workout_plan_days",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_plan_day", columnNames = {"plan_id", "day_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class WorkoutPlanDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private WorkoutPlan plan;

    @Column(name = "day_number", nullable = false)
    private short dayNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "rest_day", nullable = false)
    private boolean restDay = false;

    @OneToMany(mappedBy = "workoutPlanDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<WorkoutPlanExercise> planExercises = new ArrayList<>();
}
