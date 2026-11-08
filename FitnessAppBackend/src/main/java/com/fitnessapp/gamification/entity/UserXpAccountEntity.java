package com.fitnessapp.gamification.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "app_user_xp_accounts")
@Getter
@NoArgsConstructor
public class UserXpAccountEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    @Column(name = "total_xp", nullable = false)
    private long totalXp;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    public UserXpAccountEntity(UserEntity user) { this.user = user; this.updatedAt = Instant.now(); }
    public void addXp(long amount) {
        if (amount < 0) throw new IllegalArgumentException("XP cannot be negative");
        this.totalXp = Math.addExact(this.totalXp, amount);
        this.updatedAt = Instant.now();
    }
}
