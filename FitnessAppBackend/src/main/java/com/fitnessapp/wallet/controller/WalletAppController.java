package com.fitnessapp.wallet.controller;

import com.fitnessapp.wallet.service.WalletService;
import com.fitnessapp.wallet.model.dto.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.WalletTransactionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

public class WalletAppController {

    private final WalletService service;

    @GetMapping
    public ResponseEntity<WalletBalanceResponseDto> getBalance(Authentication authentication) {
        return ResponseEntity.ok(service.getBalance(authentication.getName()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                service.getTransactions(authentication.getName(), pageable(page, size))
        );
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
