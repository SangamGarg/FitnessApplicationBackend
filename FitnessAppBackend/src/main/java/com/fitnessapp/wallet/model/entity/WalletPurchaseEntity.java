package com.fitnessapp.wallet.model.entity;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.wallet.model.PurchaseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_user_wallet_purchases", indexes = {
        @Index(name = "idx_wallet_purchase_user", columnList = "user_id"),
        @Index(name = "idx_wallet_purchase_transaction_id", columnList = "transaction_id", unique = true)
})
public class WalletPurchaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private WalletPackageEntity walletPackage;

    @Column(name = "amount_paid", nullable = false)
    private double amountPaid;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId; // External transaction ID from payment gateway

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseStatus status = PurchaseStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
}
