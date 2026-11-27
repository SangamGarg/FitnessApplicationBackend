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
        log.info("Request received to fetch wallet home data");
        return ResponseEntity.ok(service.getWalletHome());
    }

    @GetMapping("/rules")
    public ResponseEntity<WalletRulesResponseDto> getWalletRules() {
        log.info("Request received to fetch wallet rules");
        return ResponseEntity.ok(service.getWalletRules());
    }

    @GetMapping("/earn")
    // THIS ENDPOINT IS FOR REFERRALS
    public ResponseEntity<WalletEarnResponseDto> getWalletEarn() {
        log.info("Request received to fetch wallet earn/referral information");
        return ResponseEntity.ok(service.getWalletEarn());
    }

    @GetMapping("/buy")
    // THIS ENDPOINT IS FOR BUY COINS
    public ResponseEntity<WalletBuyResponseDto> getWalletBuyInfo() {
        log.info("Request received to fetch wallet coin purchase information");
        return ResponseEntity.ok(service.getWalletBuyInfo());
    }

    @PostMapping("/buy/initiate")
    public ResponseEntity<Void> initiatePurchase(
            @RequestParam Long packageId,
            @RequestParam String transactionId) {

        log.info("Request received to initiate wallet coin purchase. packageId={}, transactionId={}",
                packageId, transactionId);

        service.initiatePurchase(packageId, transactionId);

        log.info("Wallet coin purchase initiation processed successfully. transactionId={}", transactionId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/buy/complete/{transactionId}")
    public ResponseEntity<Void> completePurchase(
            @PathVariable String transactionId,
            @RequestParam boolean success) {

        log.info("Request received for wallet purchase complete. transactionId={}, success={}",
                transactionId, success);

        service.completePurchase(transactionId, success);

        log.info("Wallet purchase complete processed successfully. transactionId={}", transactionId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponseDto> getBalance() {
        log.info("Request received to fetch wallet balance");
        return ResponseEntity.ok(service.getBalance());
    }

    @GetMapping("/transactions")
    // THIS IS FOR GETTING THE HISTORY
    public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Request received to fetch wallet transactions. page={}, size={}", page, size);

        return ResponseEntity.ok(service.getTransactions(page, size));
    }

    @GetMapping("/cashout")
    public ResponseEntity<WalletCashoutResponseDto> getWalletCashoutInfo() {
        log.info("Request received to fetch wallet cashout information");
        return ResponseEntity.ok(service.getWalletCashoutInfo());
    }

    @PostMapping("/cashout/request")
    public ResponseEntity<Void> requestCashout(@RequestBody WalletCashoutRequestDto request) {

        log.info("Request received to submit wallet cashout request");

        service.requestCashout(request);

        log.info("Wallet cashout request processed successfully");

        return ResponseEntity.ok().build();
    }
}