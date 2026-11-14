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
public class WalletCashoutResponseDto {
    private String title;
    private String subtitle;
    private String description;
    private long minCoinsRequired;
    private double conversionRate; // coins per rupee
    private List<CashoutMethodDto> methods;

    @Data
    @AllArgsConstructor
    public static class CashoutMethodDto {
        private String type;
        private String name;
        private String icon;
        private String placeholder;
    }
}
