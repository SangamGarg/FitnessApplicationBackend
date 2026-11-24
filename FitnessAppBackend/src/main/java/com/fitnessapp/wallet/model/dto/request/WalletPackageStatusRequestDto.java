package com.fitnessapp.wallet.model.dto.request;

import jakarta.validation.constraints.NotNull;

public record WalletPackageStatusRequestDto(@NotNull Boolean active) {
}
