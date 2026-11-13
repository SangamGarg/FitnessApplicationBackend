package com.fitnessapp.wallet.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_wallet_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRulesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder.Default
    private boolean active = true;
}
