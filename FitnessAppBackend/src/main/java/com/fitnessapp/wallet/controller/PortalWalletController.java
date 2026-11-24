package com.fitnessapp.wallet.controller;

import com.fitnessapp.wallet.model.dto.request.AppUserForWalletResponseDto;
import com.fitnessapp.wallet.model.dto.request.WalletAdjustmentRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletRuleRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import com.fitnessapp.wallet.model.entity.WalletRulesEntity;
import com.fitnessapp.wallet.service.PortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_ADMIN_WALLET_ENDPOINT;

@RestController
@RequestMapping(PORTAL_ADMIN_WALLET_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class PortalWalletController {

    private final PortalService portalService;

    @GetMapping("/users")
    public ResponseEntity<List<AppUserForWalletResponseDto>> getAppUsers() {
        log.info("Fetching app user for wallet portal");
        return ResponseEntity.ok(portalService.getAppUsers());
    }


    @GetMapping("/user/balance")
    public ResponseEntity<WalletBalanceResponseDto> getUserBalance(
            @RequestParam String emailId
    ) {
        return ResponseEntity.ok(portalService.getBalance(emailId));
    }

    @GetMapping("/user/transactions")
    public ResponseEntity<Page<WalletTransactionResponseDto>> getTransactions(
            @RequestParam String emailId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(portalService.getTransactions(emailId, pageable(page, size)));
    }

    @PostMapping("/user/adjust")
    public ResponseEntity<WalletTransactionResponseDto> adjust(
            @RequestParam String emailId,
            @Valid @RequestBody WalletAdjustmentRequestDto request) {
        return ResponseEntity.ok(portalService.adjustAsAdmin(emailId, request));
    }

    @GetMapping("/rules")
    public ResponseEntity<List<WalletRulesEntity>> getAllRules() {
        return ResponseEntity.ok(portalService.getAllWalletRules());
    }

    @PostMapping("/rules")
    public ResponseEntity<WalletRulesEntity> createRule(@Valid @RequestBody WalletRuleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portalService.saveWalletRule(request));
    }

    @PatchMapping("/rules/{id}")
    public ResponseEntity<WalletRulesEntity> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody WalletRuleRequestDto request
    ) {
        return ResponseEntity.ok(portalService.updateWalletRule(id, request));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        portalService.deleteWalletRule(id);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/package")


    @PostMapping("/package")

    @DeleteMapping("/package")

    @PatchMapping("/package")


    @PatchMapping("/package/status")



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
