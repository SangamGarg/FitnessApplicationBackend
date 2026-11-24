package com.fitnessapp.wallet.model.dto.request;

import com.fitnessapp.wallet.model.WalletCashoutStatus;
import jakarta.validation.constraints.NotNull;

public record WalletCashoutStatusUpdateRequestDto(
        @NotNull WalletCashoutStatus status,
        String transactionId,
        String remarks
) {
}
