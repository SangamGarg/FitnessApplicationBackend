package com.fitnessapp.gamification.game.model;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_game_sessions", uniqueConstraints = @UniqueConstraint(name = "uk_game_session_user_request", columnNames = {"user_id", "client_request_id"}), indexes = @Index(name = "idx_game_user_started", columnList = "user_id,started_at"))
@Getter
@NoArgsConstructor
public class GameSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false, length = 40)
    private GameType gameType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameSessionStatus status = GameSessionStatus.IN_PROGRESS;
    @Column(name = "client_request_id", nullable = false)
    private UUID clientRequestId;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "reps_detected")
    private Integer repsDetected;
    @Column(name = "hold_seconds")
    private Integer holdSeconds;
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    @Column(name = "score")
    private Long score;
    @Column(name = "tracker_version", length = 80)
    private String trackerVersion;
    @Column(name = "model_confidence")
    private Double modelConfidence;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public GameSessionEntity(UserEntity u, GameType t, UUID requestId) {
        user = u;
        gameType = t;
        clientRequestId = requestId;
        startedAt = Instant.now();
        createdAt = startedAt;
    }

    public void complete(int reps, Integer holdSeconds, int duration, long score, String version, Double confidence) {
        status = GameSessionStatus.COMPLETED;
        completedAt = Instant.now();
        repsDetected = reps;
        this.holdSeconds = holdSeconds;
        durationSeconds = duration;
        this.score = score;
        trackerVersion = version;
        modelConfidence = confidence;
    }
}
