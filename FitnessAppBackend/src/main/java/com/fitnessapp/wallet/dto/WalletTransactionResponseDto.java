package com.fitnessapp.wallet.dto;

import com.fitnessapp.wallet.WalletTransactionType;

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
