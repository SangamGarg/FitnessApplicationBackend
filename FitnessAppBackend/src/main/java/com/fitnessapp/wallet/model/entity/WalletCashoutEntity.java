package com.fitnessapp.wallet.model.entity;

import com.fitnessapp.wallet.model.WalletCashoutStatus;
import com.fitnessapp.wallet.model.WalletCashoutType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_user_wallet_cashouts")
public class WalletCashoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private com.fitnessapp.appAuth.model.entity.UserEntity user;

    @Column(nullable = false)
    private long amountCoins;

    @Column(nullable = false)
    private double amountRupees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletCashoutType type;

    @Column(nullable = false, length = 500)
    private String details; // UPI ID or Bank Details JSON

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletCashoutStatus status;

    @Column(unique = true, length = 100)
    private String transactionId;

    private String remarks;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = WalletCashoutStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
