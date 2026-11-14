package com.fitnessapp.wallet.model.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletHomeResponseDto {
    private String title;
    private String subtitle;
    private String description;
    private BalanceBoxDto balanceBox;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceBoxDto {
        private String title;
        private String threshouldReqString;
        private String eligibleString;
        private long currentBalance;
        private double thresholdProgress; // e.g. 0.99 for 99%
        private double currencyValue; // value in rupees
    }
}
