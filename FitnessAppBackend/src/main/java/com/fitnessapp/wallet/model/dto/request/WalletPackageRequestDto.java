package com.fitnessapp.wallet.model.dto.request;

import lombok.Data;

@Data
public class WalletPackageRequestDto {
    private String name;
    private Long amountCoins;
    private Long bonusCoins;
    private Double price;
    private String currency;
    private String tag;
    private String description;
    private Boolean active;
    private Integer displayOrder;
}
