package com.fitnessapp.wallet.model.dto;

import java.time.Instant;

public record WalletBalanceResponseDto(String userId, long balanceCoins, Instant updatedAt) {
}
