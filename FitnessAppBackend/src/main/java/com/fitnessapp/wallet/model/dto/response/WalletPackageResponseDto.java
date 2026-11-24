package com.fitnessapp.wallet.model.dto.response;

import com.fitnessapp.wallet.model.entity.WalletPackageEntity;

public record WalletPackageResponseDto(
        Long id,
        String name,
        long amountCoins,
        long bonusCoins,
        double price,
        String currency,
        String tag,
        String description,
        boolean active,
        int displayOrder
) {
    public static WalletPackageResponseDto from(WalletPackageEntity entity) {
        return new WalletPackageResponseDto(
                entity.getId(), entity.getName(), entity.getAmountCoins(), entity.getBonusCoins(),
                entity.getPrice(), entity.getCurrency(), entity.getTag(), entity.getDescription(),
                entity.isActive(), entity.getDisplayOrder()
        );
    }
}
