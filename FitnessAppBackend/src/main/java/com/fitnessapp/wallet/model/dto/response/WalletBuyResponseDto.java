package com.fitnessapp.wallet.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBuyResponseDto {
    private String title;
    private String subtitle;
    private String description;
    private List<WalletPackageDto> packages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletPackageDto {
        private Long id;
        private String name;
        private long amountCoins;
        private long bonusCoins;
        private double price;
        private String currency; // e.g., INR
        private String tag; // e.g., Best Value, Popular
        private String description;
    }
}
