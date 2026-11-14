package com.fitnessapp.wallet.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "app_user_wallet_referrals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletReferralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "referral_code", nullable = false, unique = true, length = 50)
    private String referralCode;

    @Column(name = "total_earned_mp_coins")
    @Builder.Default
    private long totalEarnedMpCoins = 0;

    @Column(name = "total_friends_invited")
    @Builder.Default
    private int totalFriendsInvited = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
