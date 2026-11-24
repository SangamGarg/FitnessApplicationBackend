package com.fitnessapp.wallet.model.dto.response;

import com.fitnessapp.wallet.model.PurchaseStatus;
import com.fitnessapp.wallet.model.entity.WalletPurchaseEntity;

import java.time.Instant;

public record PortalWalletPurchaseResponseDto(
        Long id, String userId, String userEmail, Long packageId, String packageName,
        long amountCoins, long bonusCoins, double amountPaid, String currency,
        String transactionId, PurchaseStatus status, Instant createdAt, Instant updatedAt
) {
    public static PortalWalletPurchaseResponseDto from(WalletPurchaseEntity entity) {
        var walletPackage = entity.getWalletPackage();
        return new PortalWalletPurchaseResponseDto(entity.getId(), entity.getUser().getId(),
                entity.getUser().getEmail(), walletPackage.getId(), walletPackage.getName(),
                walletPackage.getAmountCoins(), walletPackage.getBonusCoins(), entity.getAmountPaid(),
                entity.getCurrency(), entity.getTransactionId(), entity.getStatus(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
