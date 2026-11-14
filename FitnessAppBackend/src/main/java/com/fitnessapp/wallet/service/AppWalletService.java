package com.fitnessapp.wallet.service;

import com.fitnessapp.wallet.model.dto.request.WalletCashoutRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletBuyResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletCashoutResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletEarnResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletHomeResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletRulesResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import org.springframework.data.domain.Page;

public interface AppWalletService {

    WalletHomeResponseDto getWalletHome();

    WalletRulesResponseDto getWalletRules();

    WalletEarnResponseDto getWalletEarn();

    WalletBuyResponseDto getWalletBuyInfo();

    void initiatePurchase(Long packageId, String transactionId);

    void completePurchase(String transactionId, boolean success);

    WalletBalanceResponseDto getBalance();

    Page<WalletTransactionResponseDto> getTransactions(int page, int size);

    WalletCashoutResponseDto getWalletCashoutInfo();

    void requestCashout(WalletCashoutRequestDto request);

    void linkReferral(String newUserId, String referralCode);

    void processReferralReward(String referredUserId);

}