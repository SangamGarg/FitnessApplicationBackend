package com.fitnessapp.exercise.app.model.entity.entityNew;

import com.fitnessapp.exercise.app.model.TrackingMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "workout_set_logs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_exercise_set_number", columnNames = {"exercise_log_id", "set_number"})
        }
)
@Getter
@Setter
@NoArgsConstructor
class WorkoutSetLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_log_id", nullable = false)
    private WorkoutExerciseLog exerciseLog;

    @Column(name = "set_number", nullable = false)
    private short setNumber;

    @Column(name = "reps_completed")
    private Integer repsCompleted;

    @Column(name = "weight_kg", precision = 7, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_method", nullable = false, length = 20)
    private TrackingMethod trackingMethod;

    @Column(name = "rep_confidence", precision = 5, scale = 4)
    private BigDecimal repConfidence;

    @Column(name = "completed_at")
    private Instant completedAt;
}
