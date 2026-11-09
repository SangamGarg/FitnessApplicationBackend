package com.fitnessapp.exercise.app.model.entity.entityNew;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "exercise_muscles")
@Getter
@Setter
@NoArgsConstructor
public class ExerciseMuscle {

    @EmbeddedId
    private ExerciseMuscleId id = new ExerciseMuscleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("exerciseId")
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("muscleGroupId")
    @JoinColumn(name = "muscle_group_id", nullable = false)
    private MuscleGroup muscleGroup;

    @Column(nullable = false, length = 20)
    private String role; // PRIMARY, SECONDARY

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class ExerciseMuscleId implements Serializable {
        private Long exerciseId;
        private Long muscleGroupId;
    }
}
