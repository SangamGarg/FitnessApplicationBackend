package com.fitnessapp.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "wallet_transactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_wallet_transaction_idempotency",
                columnNames = {"wallet_id", "idempotency_key"}
        ),
        indexes = {
                @Index(name = "idx_wallet_transaction_wallet_created", columnList = "wallet_id, created_at")
        })
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wallet_transaction_account"))
    private WalletAccountEntity wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private WalletTransactionType type;

    @Column(name = "amount_coins", nullable = false)
    private long amountCoins;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(length = 160)
    private String reference;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "actor_email", nullable = false, length = 254)
    private String actorEmail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WalletTransactionEntity(
            WalletAccountEntity wallet,
            WalletTransactionType type,
            long amountCoins,
            long balanceAfter,
            String reason,
            String reference,
            String idempotencyKey,
            String actorEmail
    ) {
        this.wallet = wallet;
        this.type = type;
        this.amountCoins = amountCoins;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.reference = reference;
        this.idempotencyKey = idempotencyKey;
        this.actorEmail = actorEmail;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
