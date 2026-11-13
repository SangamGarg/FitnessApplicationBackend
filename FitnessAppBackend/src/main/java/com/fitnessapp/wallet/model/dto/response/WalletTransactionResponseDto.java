package com.fitnessapp.wallet.model.dto.response;

import com.fitnessapp.wallet.model.WalletTransactionType;

import java.time.Instant;

public record WalletTransactionResponseDto(
        Long id,
        WalletTransactionType type,
        long amountCoins,
        long balanceAfter,
        String reason,
        String reference,
        String idempotencyKey,
        Instant createdAt
) {
}
