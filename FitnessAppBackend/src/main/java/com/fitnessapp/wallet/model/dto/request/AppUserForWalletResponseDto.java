package com.fitnessapp.wallet.model.dto.request;

import com.fitnessapp.appAuth.model.entity.UserEntity;

public record AppUserForWalletResponseDto(String name,
                                          String email
) {
    public static AppUserForWalletResponseDto from(UserEntity e) {
        return new AppUserForWalletResponseDto(
                e.getUserDetailsEntity().getName(), e.getEmail());
    }
}
