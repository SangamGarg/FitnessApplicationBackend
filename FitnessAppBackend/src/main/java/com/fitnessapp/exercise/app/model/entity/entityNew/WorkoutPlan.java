package com.fitnessapp.exercise.app.model.entity.entityNew;

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
    name = "workout_plans",
    indexes = {
        @Index(name = "idx_workout_plans_catalog", columnList = "is_published, goal, difficulty")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_key", nullable = false, unique = true, length = 80)
    private String planKey;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String goal;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(name = "duration_weeks", nullable = false)
    private short durationWeeks;

    @Column(name = "days_per_week", nullable = false)
    private short daysPerWeek;

    @Column(name = "estimated_session_minutes")
    private Short estimatedSessionMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "equipment_requirements", columnDefinition = "jsonb")
    private String equipmentRequirements = "[]";

    @Column(nullable = false, length = 20)
    private String visibility = "PUBLIC";

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "is_published", nullable = false)
    private boolean published = false;

    @Column(nullable = false)
    private int version = 1;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    private List<WorkoutPlanDay> days = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
