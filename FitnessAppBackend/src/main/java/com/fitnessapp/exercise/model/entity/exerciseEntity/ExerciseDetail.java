package com.fitnessapp.exercise.model.entity.exerciseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exercise_detail")
@Getter
@Setter
@NoArgsConstructor
public class ExerciseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false, unique = true)
    private Exercise exercise;

    @Column(columnDefinition = "TEXT")
    private String setup;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "safety_notes", columnDefinition = "TEXT")
    private String safetyNotes;

    @Column(name = "common_mistakes", columnDefinition = "TEXT")
    private String commonMistakes;
}
