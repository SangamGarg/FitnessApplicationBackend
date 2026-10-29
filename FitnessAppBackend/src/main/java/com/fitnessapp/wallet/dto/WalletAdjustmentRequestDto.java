package com.fitnessapp.wallet.dto;

import com.fitnessapp.wallet.WalletTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WalletAdjustmentRequestDto(
        @NotNull WalletTransactionType type,
        @Positive long amountCoins,
        @NotBlank @Size(max = 300) String reason,
        @Size(max = 160) String reference,
        @NotBlank @Size(max = 100) String idempotencyKey
) {
}
