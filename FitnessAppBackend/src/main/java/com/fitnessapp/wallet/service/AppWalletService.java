package com.fitnessapp.wallet.service;

import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletHomeResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletRulesResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppWalletService {

    WalletHomeResponseDto getWalletHome();

    WalletRulesResponseDto getWalletRules();


    WalletBalanceResponseDto getBalance();

    Page<WalletTransactionResponseDto> getTransactions(Pageable pageable);

}