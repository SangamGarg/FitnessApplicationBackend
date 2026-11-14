package com.fitnessapp.wallet.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "app_user_wallet_referral_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletReferralHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referrer_id", nullable = false)
    private UserEntity referrer;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "referred_user_id", nullable = false, unique = true)
    private UserEntity referredUser;

    @Column(nullable = false)
    private String status; // e.g., PENDING, COMPLETED

    @Column(name = "earned_coins")
    private long earnedCoins;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
