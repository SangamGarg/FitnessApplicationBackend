package com.fitnessapp.exercise.app.model.entity.entityNew;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "muscle_groups")
@Getter
@Setter
@NoArgsConstructor
public class MuscleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "muscle_key", nullable = false, unique = true, length = 80)
    private String muscleKey;

    @Column(nullable = false, length = 100)
    private String name;
}
