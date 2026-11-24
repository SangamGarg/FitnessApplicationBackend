package com.fitnessapp.wallet.model.dto.request;

import com.fitnessapp.wallet.model.WalletCashoutStatus;
import com.fitnessapp.wallet.model.WalletCashoutType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Data
public class WalletCashoutFilterRequestDto {
    private String emailId;
    private WalletCashoutStatus status;
    private WalletCashoutType type;
    private String transactionId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant createdFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant createdTo;
}
