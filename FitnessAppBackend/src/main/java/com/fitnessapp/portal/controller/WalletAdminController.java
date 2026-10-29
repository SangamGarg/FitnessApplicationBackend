package com.fitnessapp.portal.controller;

import com.fitnessapp.wallet.WalletService;
import com.fitnessapp.wallet.dto.WalletAdjustmentRequestDto;
import com.fitnessapp.wallet.dto.WalletBalanceResponseDto;
import com.fitnessapp.wallet.dto.WalletTransactionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_BASE_URL;

@RestController
@RequestMapping(PORTAL_BASE_URL + "/admin/wallets")
@Tag(name = "Admin - Wallets", description = "Inspect wallet balances and record audited coin adjustments")
@SecurityRequirement(name = "bearerAuth")
public class WalletAdminController {

    private final WalletService service;

    public WalletAdminController(WalletService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a mobile user's wallet balance")
    public ResponseEntity<WalletBalanceResponseDto> getBalance(@PathVariable String userId) {
        return ResponseEntity.ok(service.getBalance(userId));
    }

    @GetMapping("/{userId}/transactions")
    @Operation(summary = "Get a mobile user's wallet ledger")
    public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.getTransactions(userId, pageable(page, size)));
    }

    @PostMapping("/{userId}/transactions")
    @Operation(summary = "Credit or debit a wallet with an idempotent audited adjustment")
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
