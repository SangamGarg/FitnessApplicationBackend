package com.fitnessapp.wallet.model.dto.response;

import java.time.Instant;

public record WalletBalanceResponseDto(String userId, long balanceCoins, Instant updatedAt) {
}
