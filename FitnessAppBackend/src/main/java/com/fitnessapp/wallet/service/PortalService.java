package com.fitnessapp.wallet.service;

import com.fitnessapp.wallet.model.dto.request.AppUserForWalletResponseDto;
import com.fitnessapp.wallet.model.dto.request.WalletAdjustmentRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletRuleRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletPackageRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletPackageResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import com.fitnessapp.wallet.model.dto.request.WalletCashoutFilterRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletCashoutStatusUpdateRequestDto;
import com.fitnessapp.wallet.model.dto.response.PortalWalletCashoutResponseDto;
import com.fitnessapp.wallet.model.dto.response.PortalWalletPurchaseResponseDto;
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

    List<WalletPackageResponseDto> getWalletPackages();
    WalletPackageResponseDto getWalletPackage(Long id);
    WalletPackageResponseDto createWalletPackage(WalletPackageRequestDto request);
    WalletPackageResponseDto updateWalletPackage(Long id, WalletPackageRequestDto request);
    WalletPackageResponseDto patchWalletPackage(Long id, WalletPackageRequestDto request);
    WalletPackageResponseDto updateWalletPackageStatus(Long id, boolean active);
    void deleteWalletPackage(Long id);
    Page<PortalWalletCashoutResponseDto> getCashouts(WalletCashoutFilterRequestDto filters, Pageable pageable);
    PortalWalletCashoutResponseDto updateCashoutStatus(Long id, WalletCashoutStatusUpdateRequestDto request);
    Page<PortalWalletPurchaseResponseDto> getUserPurchases(String emailId, Pageable pageable);
}
