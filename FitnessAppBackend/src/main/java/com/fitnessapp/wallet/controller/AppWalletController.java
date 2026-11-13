package com.fitnessapp.wallet.controller;

import com.fitnessapp.wallet.model.dto.response.WalletHomeResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletRulesResponseDto;
import com.fitnessapp.wallet.service.AppWalletService;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_WALLET_ENDPOINT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(APP_WALLET_ENDPOINT)

public class AppWalletController {

    private final AppWalletService service;

    @GetMapping("/rules")
    public ResponseEntity<WalletRulesResponseDto> getWalletRules() {
        return ResponseEntity.ok(service.getWalletRules());
    }
      @GetMapping("/rules")
    public ResponseEntity<WalletHomeResponseDto> getWalletHome() {
        return ResponseEntity.ok(service.getWalletHome());
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponseDto> getBalance() {
        return ResponseEntity.ok(service.getBalance());
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getTransactions(pageable(page, size)));
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
