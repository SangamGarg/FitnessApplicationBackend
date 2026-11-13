package com.fitnessapp.wallet.controller;

import com.fitnessapp.wallet.model.dto.request.WalletAdjustmentRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import com.fitnessapp.wallet.service.PortalServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_ADMIN_WALLET_ENDPOINT;

@RestController
@RequestMapping(PORTAL_ADMIN_WALLET_ENDPOINT)
@RequiredArgsConstructor
public class PortalWalletController {

    private final PortalServiceImpl service;


    @GetMapping("/{userId}")
    public ResponseEntity<WalletBalanceResponseDto> getBalance(@PathVariable String userId) {
        return ResponseEntity.ok(service.getBalance(userId));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.getTransactions(userId, pageable(page, size)));
    }

    @PostMapping("/{userId}/transactions")
    public ResponseEntity<WalletTransactionResponseDto> adjust(
            @PathVariable String userId,
            @Valid @RequestBody WalletAdjustmentRequestDto request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(service.adjustAsAdmin(userId, authentication.getName(), request));
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
