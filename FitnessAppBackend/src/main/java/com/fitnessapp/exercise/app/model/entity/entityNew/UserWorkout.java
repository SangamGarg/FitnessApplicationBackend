package com.fitnessapp.exercise.app.model.entity.entityNew;

import com.fitnessapp.exercise.app.model.WorkoutSessionMode;
import com.fitnessapp.exercise.app.model.WorkoutStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "user_workouts",
    indexes = {
        @Index(name = "idx_user_workouts_history", columnList = "user_id, started_at DESC")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_client_request", columnNames = {"user_id", "client_request_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class UserWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "workout_plan_id")
    private Long workoutPlanId;

    @Column(name = "plan_day_id")
    private Long planDayId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_mode", nullable = false, length = 20)
    private WorkoutSessionMode sessionMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkoutStatus status = WorkoutStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "client_request_id")
    private UUID clientRequestId;

    @Version
    @Column(nullable = false)
    private Integer version = 0;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<WorkoutExerciseLog> exerciseLogs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
