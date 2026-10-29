package com.fitnessapp.wallet;

import com.fitnessapp.wallet.dto.WalletBalanceResponseDto;
import com.fitnessapp.wallet.dto.WalletTransactionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/v1/app/wallet")
@Tag(name = "App - Wallet", description = "Read the authenticated user's coin balance and wallet ledger")
@SecurityRequirement(name = "bearerAuth")
public class WalletAppController {

    private final WalletService service;

    public WalletAppController(WalletService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get the authenticated user's wallet balance")
    public ResponseEntity<WalletBalanceResponseDto> getBalance(Authentication authentication) {
        return ResponseEntity.ok(service.getBalance(authentication.getName()));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get the authenticated user's wallet transaction history")
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
