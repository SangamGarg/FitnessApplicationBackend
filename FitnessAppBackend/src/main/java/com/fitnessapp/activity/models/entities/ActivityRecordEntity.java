package com.fitnessapp.activity.models.entities;

import com.fitnessapp.activity.models.enums.ActivitySource;
import com.fitnessapp.activity.models.enums.ActivityStatus;
import com.fitnessapp.activity.models.enums.ActivityType;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "app_activity_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_activity_user_source_record",
                        columnNames = {"user_id", "source", "source_record_id"}
                ),
                @UniqueConstraint(
                        name = "uk_activity_user_dedupe_key",
                        columnNames = {"user_id", "deduplication_key"}
                )
        },
        indexes = {
                @Index(name = "idx_activity_user_started", columnList = "user_id, started_at"),
                @Index(name = "idx_activity_user_status_started", columnList = "user_id, activity_status, started_at"),
                @Index(name = "idx_activity_user_type_started", columnList = "user_id, activity_type, started_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ActivityRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_activity_records_user"))
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivitySource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_status", nullable = false, length = 20)
    private ActivityStatus status;

    @Column(name = "activity_name", nullable = false, length = 120)
    private String activityName;

    /**
     * Stable ID supplied by the originating system.
     * For APP records this is the clientActivityId UUID.
     * For health records this is the Health Connect / HealthKit record ID.
     */
    @Column(name = "source_record_id", nullable = false, length = 200)
    private String sourceRecordId;

    /**
     * Optional shared identifier used when the same MusclePlay activity is
     * written to a health platform and later read back by the application.
     */
    @Column(name = "deduplication_key", length = 120)
    private String deduplicationKey;

    @Column(name = "origin_app_id", length = 255)
    private String originAppId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "calories_burned", precision = 10, scale = 2)
    private BigDecimal caloriesBurned;

    @Column(name = "distance_meters", precision = 12, scale = 2)
    private BigDecimal distanceMeters;

    @Column(name = "steps")
    private Long steps;

    /**
     * Only canonical records contribute to dashboard aggregates.
     * Duplicate records remain stored for traceability.
     */
    @Column(name = "included_in_summary", nullable = false)
    private boolean includedInSummary = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_id",
            foreignKey = @ForeignKey(name = "fk_activity_duplicate_of"))
    private ActivityRecordEntity duplicateOf;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
