package com.fitnessapp.wallet.controller;

import com.fitnessapp.wallet.model.dto.request.AppUserForWalletResponseDto;
import com.fitnessapp.wallet.model.dto.request.WalletAdjustmentRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletRuleRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletPackageRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletPackageStatusRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletPackageResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import com.fitnessapp.wallet.model.dto.request.WalletCashoutFilterRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletCashoutStatusUpdateRequestDto;
import com.fitnessapp.wallet.model.dto.response.PortalWalletCashoutResponseDto;
import com.fitnessapp.wallet.model.dto.response.PortalWalletPurchaseResponseDto;
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


    @GetMapping("/cashouts")
    public ResponseEntity<Page<PortalWalletCashoutResponseDto>> getCashouts(
            @ModelAttribute WalletCashoutFilterRequestDto filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(portalService.getCashouts(filters, pageable(page, size)));
    }

    @PatchMapping("/cashouts/{id}/status")
    public ResponseEntity<PortalWalletCashoutResponseDto> updateCashoutStatus(
            @PathVariable Long id, @Valid @RequestBody WalletCashoutStatusUpdateRequestDto request) {
        return ResponseEntity.ok(portalService.updateCashoutStatus(id, request));
    }

    @GetMapping("/users/purchases")
    public ResponseEntity<Page<PortalWalletPurchaseResponseDto>> getUserPurchases(
            @RequestParam String emailId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(portalService.getUserPurchases(emailId, pageable(page, size)));
    }

    @GetMapping("/packages")
    public ResponseEntity<List<WalletPackageResponseDto>> getWalletPackages() {
        return ResponseEntity.ok(portalService.getWalletPackages());
    }

    @GetMapping("/packages/{id}")
    public ResponseEntity<WalletPackageResponseDto> getWalletPackage(@PathVariable Long id) {
        return ResponseEntity.ok(portalService.getWalletPackage(id));
    }

    @PostMapping("/packages")
    public ResponseEntity<WalletPackageResponseDto> createWalletPackage(@RequestBody WalletPackageRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portalService.createWalletPackage(request));
    }

    @PutMapping("/packages/{id}")
    public ResponseEntity<WalletPackageResponseDto> updateWalletPackage(@PathVariable Long id, @RequestBody WalletPackageRequestDto request) {
        return ResponseEntity.ok(portalService.updateWalletPackage(id, request));
    }

    @PatchMapping("/packages/{id}")
    public ResponseEntity<WalletPackageResponseDto> patchWalletPackage(@PathVariable Long id, @RequestBody WalletPackageRequestDto request) {
        return ResponseEntity.ok(portalService.patchWalletPackage(id, request));
    }

    @PatchMapping("/packages/{id}/status")
    public ResponseEntity<WalletPackageResponseDto> updateWalletPackageStatus(@PathVariable Long id, @Valid @RequestBody WalletPackageStatusRequestDto request) {
        return ResponseEntity.ok(portalService.updateWalletPackageStatus(id, request.active()));
    }

    @DeleteMapping("/packages/{id}")
    public ResponseEntity<Void> deleteWalletPackage(@PathVariable Long id) {
        portalService.deleteWalletPackage(id);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<WalletTransactionResponseDto> adjustUserWallet(
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
