package com.fitnessapp.wallet.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_wallet_packages")
public class WalletPackageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "amount_coins", nullable = false)
    private long amountCoins;

    @Column(name = "bonus_coins", nullable = false)
    private long bonusCoins;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false, length = 10)
    private String currency; // e.g., INR

    @Column(length = 50)
    private String tag; // e.g., Best Value, Popular

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_order")
    private int displayOrder;
}
