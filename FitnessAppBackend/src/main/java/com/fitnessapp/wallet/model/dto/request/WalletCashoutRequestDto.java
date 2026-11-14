package com.fitnessapp.wallet.model.dto.request;

import com.fitnessapp.wallet.model.WalletCashoutType;
import lombok.Data;

@Data
public class WalletCashoutRequestDto {
    private long amountCoins;
    private WalletCashoutType type;
    private String details; // UPI ID or Bank Details JSON
    private String transactionId; // For idempotency
}
