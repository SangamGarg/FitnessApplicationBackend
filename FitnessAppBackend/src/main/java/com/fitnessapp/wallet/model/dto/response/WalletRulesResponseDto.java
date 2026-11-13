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
public class WalletRulesResponseDto {
    private String header;
    private String subheader;
    private List<WalletRuleDto> rules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletRuleDto {
        private String title;
        private String description;
    }
}
