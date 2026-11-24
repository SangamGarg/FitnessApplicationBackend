package com.fitnessapp.wallet.model.dto.response;

import com.fitnessapp.wallet.model.WalletCashoutStatus;
import com.fitnessapp.wallet.model.WalletCashoutType;
import com.fitnessapp.wallet.model.entity.WalletCashoutEntity;

import java.time.Instant;

public record PortalWalletCashoutResponseDto(
        Long id, String userId, String userEmail, long amountCoins, double amountRupees,
        WalletCashoutType type, String details, WalletCashoutStatus status,
        String transactionId, String remarks, Instant createdAt, Instant updatedAt
) {
    public static PortalWalletCashoutResponseDto from(WalletCashoutEntity entity) {
        return new PortalWalletCashoutResponseDto(entity.getId(), entity.getUser().getId(),
                entity.getUser().getEmail(), entity.getAmountCoins(), entity.getAmountRupees(),
                entity.getType(), entity.getDetails(), entity.getStatus(), entity.getTransactionId(),
                entity.getRemarks(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
