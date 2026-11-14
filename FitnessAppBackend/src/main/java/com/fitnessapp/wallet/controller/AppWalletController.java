package com.fitnessapp.wallet.controller;

import com.fitnessapp.wallet.model.dto.request.WalletCashoutRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletBuyResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletCashoutResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletEarnResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletHomeResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletRulesResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import com.fitnessapp.wallet.service.AppWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_WALLET_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(APP_WALLET_ENDPOINT)

public class AppWalletController {

    private final AppWalletService service;

    @GetMapping("/home")
    public ResponseEntity<WalletHomeResponseDto> getWalletHome() {
        return ResponseEntity.ok(service.getWalletHome());
    }

    @GetMapping("/rules")
    public ResponseEntity<WalletRulesResponseDto> getWalletRules() {
        return ResponseEntity.ok(service.getWalletRules());
    }

    @GetMapping("/earn")
    public ResponseEntity<WalletEarnResponseDto> getWalletEarn() {
        return ResponseEntity.ok(service.getWalletEarn());
    }

    @GetMapping("/buy")
    public ResponseEntity<WalletBuyResponseDto> getWalletBuyInfo() {
        return ResponseEntity.ok(service.getWalletBuyInfo());
    }

    @PostMapping("/buy/initiate")
    public ResponseEntity<Void> initiatePurchase(
            @RequestParam Long packageId,
            @RequestParam String transactionId) {
        service.initiatePurchase(packageId, transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/buy/callback/{transactionId}")
    public ResponseEntity<Void> paymentCallback(
            @PathVariable String transactionId,
            @RequestParam boolean success) {
        service.completePurchase(transactionId, success);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponseDto> getBalance() {
        return ResponseEntity.ok(service.getBalance());
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getTransactions(page, size));
    }

    @GetMapping("/cashout")
    public ResponseEntity<WalletCashoutResponseDto> getWalletCashoutInfo() {
        return ResponseEntity.ok(service.getWalletCashoutInfo());
    }

    @PostMapping("/cashout/request")
    public ResponseEntity<Void> requestCashout(@RequestBody WalletCashoutRequestDto request) {
        service.requestCashout(request);
        return ResponseEntity.ok().build();
    }
}
