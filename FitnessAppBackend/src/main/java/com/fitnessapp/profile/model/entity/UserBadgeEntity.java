package com.fitnessapp.profile.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "app_profile_user_badges",
        uniqueConstraints = @UniqueConstraint(name = "uk_profile_user_badge", columnNames = {"user_id", "badge_id"}),
        indexes = @Index(name = "idx_profile_user_badge_user", columnList = "user_id,badge_id"))
@Getter
@NoArgsConstructor
public class UserBadgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id", nullable = false)
    private BadgeEntity badge;

    @Column(nullable = false)
    private boolean unlocked;

    @Column(name = "unlocked_at")
    private Instant unlockedAt;

    public UserBadgeEntity(UserEntity user, BadgeEntity badge) {
        this.user = user;
        this.badge = badge;
        this.unlocked = true;
        this.unlockedAt = Instant.now();
    }
}
