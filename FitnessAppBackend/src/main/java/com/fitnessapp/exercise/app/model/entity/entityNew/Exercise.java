package com.fitnessapp.exercise.app.model.entity.entityNew;

import com.fitnessapp.exercise.app.model.ExerciseDifficulty;
import com.fitnessapp.exercise.app.model.ExerciseType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "exercises",
    indexes = {
        @Index(name = "idx_exercises_published_diff_type", columnList = "is_published, difficulty, exercise_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercise_key", nullable = false, unique = true, length = 80)
    private String exerciseKey;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String instructions = "[]";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExerciseDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 30)
    private ExerciseType exerciseType;

    @Column(name = "movement_pattern", length = 40)
    private String movementPattern;

    @Column(nullable = false, length = 40)
    private String equipment;

    @Column(name = "is_unilateral", nullable = false)
    private boolean unilateral = false;

    @Column(name = "supports_ai_tracking", nullable = false)
    private boolean supportsAiTracking = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tracking_config", columnDefinition = "jsonb")
    private String trackingConfig;

    @Column(name = "is_published", nullable = false)
    private boolean published = false;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseMuscle> exerciseMuscles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
