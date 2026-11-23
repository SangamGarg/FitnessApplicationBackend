package com.fitnessapp.gamify.challenge.model.entity;

import com.fitnessapp.gamify.challenge.model.enums.ChallengeMetric;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "app_challenges", indexes = @Index(name = "idx_challenge_active_window", columnList = "active, starts_at, ends_at"))
@Getter
@NoArgsConstructor
public class ChallengeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(length = 1000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChallengeMetric metric;
    @Column(name = "target_value", nullable = false)
    private long targetValue;
    @Column(name = "activity_type", length = 40)
    private String activityType;
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;
    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;
    @Column(name = "reward_xp", nullable = false)
    private long rewardXp;
    @Column(name = "reward_coins", nullable = false)
    private long rewardCoins;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void create() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public ChallengeEntity(String title, String description, ChallengeMetric metric, long targetValue, String activityType, Instant startsAt, Instant endsAt, long rewardXp, long rewardCoins) {
        this.title = title;
        this.description = description;
        this.metric = metric;
        this.targetValue = targetValue;
        this.activityType = activityType;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.rewardXp = rewardXp;
        this.rewardCoins = rewardCoins;
        this.active = true;
    }

    public boolean isJoinable(Instant now) {
        return active && !now.isBefore(startsAt) && now.isBefore(endsAt);
    }

    public void update(String title, String description, ChallengeMetric metric, long targetValue,
                       String activityType, Instant startsAt, Instant endsAt, long rewardXp, long rewardCoins) {
        this.title = title;
        this.description = description;
        this.metric = metric;
        this.targetValue = targetValue;
        this.activityType = activityType;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.rewardXp = rewardXp;
        this.rewardCoins = rewardCoins;
    }

    public void deactivate() {
        this.active = false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMetric(ChallengeMetric metric) {
        this.metric = metric;
    }

    public void setTargetValue(long targetValue) {
        this.targetValue = targetValue;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public void setRewardXp(long rewardXp) {
        this.rewardXp = rewardXp;
    }

    public void setRewardCoins(long rewardCoins) {
        this.rewardCoins = rewardCoins;
    }
}
