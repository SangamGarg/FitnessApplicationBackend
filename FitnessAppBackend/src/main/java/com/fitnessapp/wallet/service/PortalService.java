package com.fitnessapp.wallet.service;

import com.fitnessapp.wallet.model.dto.request.AppUserForWalletResponseDto;
import com.fitnessapp.wallet.model.dto.request.WalletAdjustmentRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletRuleRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import com.fitnessapp.wallet.model.entity.WalletRulesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PortalService {
    List<AppUserForWalletResponseDto> getAppUsers();

    WalletBalanceResponseDto getBalance(String emailId);

    Page<WalletTransactionResponseDto> getTransactions(String emailId, Pageable pageable);


    WalletTransactionResponseDto adjustAsAdmin(String emailId, WalletAdjustmentRequestDto request);

    List<WalletRulesEntity> getAllWalletRules();

    WalletRulesEntity saveWalletRule(WalletRuleRequestDto request);

    void deleteWalletRule(Long id);

    WalletRulesEntity updateWalletRule(Long id, WalletRuleRequestDto request);
}
