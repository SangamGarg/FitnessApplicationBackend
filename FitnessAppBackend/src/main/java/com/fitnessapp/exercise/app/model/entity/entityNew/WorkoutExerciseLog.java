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
    name = "workout_exercise_logs",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_workout_exercise_order", columnNames = {"workout_id", "sort_order"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class WorkoutExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private UserWorkout workout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_method", nullable = false, length = 20)
    private TrackingMethod trackingMethod;

    @Column(name = "tracker_version", length = 50)
    private String trackerVersion;

    @OneToMany(mappedBy = "exerciseLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private List<WorkoutSetLog> setLogs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
