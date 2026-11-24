package com.fitnessapp.wallet.service;

import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.portal.audit.service.PortalAuditService;
import com.fitnessapp.wallet.model.WalletTransactionType;
import com.fitnessapp.wallet.model.dto.request.AppUserForWalletResponseDto;
import com.fitnessapp.wallet.model.dto.request.WalletAdjustmentRequestDto;
import com.fitnessapp.wallet.model.dto.request.WalletRuleRequestDto;
import com.fitnessapp.wallet.model.dto.response.WalletBalanceResponseDto;
import com.fitnessapp.wallet.model.dto.response.WalletTransactionResponseDto;
import com.fitnessapp.wallet.model.entity.WalletAccountEntity;
import com.fitnessapp.wallet.model.entity.WalletRulesEntity;
import com.fitnessapp.wallet.model.entity.WalletTransactionEntity;
import com.fitnessapp.wallet.repo.WalletAccountRepository;
import com.fitnessapp.wallet.repo.WalletRulesRepository;
import com.fitnessapp.wallet.repo.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalServiceImpl implements PortalService {

    private final UserRepo userRepo;
    private final WalletAccountRepository wallets;
    private final WalletTransactionRepository transactions;
    private final WalletRulesRepository rulesRepo;
    private final PortalAuditService audit;


    @Override
    @Transactional(readOnly = true)
    public List<AppUserForWalletResponseDto> getAppUsers() {
        List<UserEntity> list = userRepo.findAll();
        return list.stream().map(AppUserForWalletResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<WalletRulesEntity> getAllWalletRules() {
        return rulesRepo.findAll();
    }

    @Transactional
    @Override
    public WalletRulesEntity saveWalletRule(WalletRuleRequestDto request) {
        WalletRulesEntity rule = WalletRulesEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .active(request.isActive())
                .build();
        rule = rulesRepo.save(rule);
        audit.record("WALLET_RULE_CREATE", "WALLET", "SYSTEM", "Created rule: " + rule.getTitle());
        return rule;
    }

    @Transactional
    @Override
    public WalletRulesEntity updateWalletRule(Long id, WalletRuleRequestDto request) {
        WalletRulesEntity rule = rulesRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet rule not found"));

        rule.setTitle(request.getTitle());
        rule.setDescription(request.getDescription());
        rule.setDisplayOrder(request.getDisplayOrder());
        rule.setActive(request.isActive());

        rule = rulesRepo.save(rule);
        audit.record("WALLET_RULE_UPDATE", "WALLET", "SYSTEM", "Updated rule: " + id);
        return rule;
    }

    @Transactional
    @Override
    public void deleteWalletRule(Long id) {
        if (!rulesRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet rule not found");
        }
        rulesRepo.deleteById(id);
        audit.record("WALLET_RULE_DELETE", "WALLET", "SYSTEM", "Deleted rule: " + id);
    }

    @Transactional(readOnly = true)
    @Override
    public WalletBalanceResponseDto getBalance(String emailId) {
        String userId = getUserIdFromEmail(emailId);
        return wallets.findByUserId(userId)
                .map(wallet -> new WalletBalanceResponseDto(userId, wallet.getBalanceCoins(), wallet.getUpdatedAt()))
                .orElseGet(() -> new WalletBalanceResponseDto(userId, 0, null));
    }


    @Transactional(readOnly = true)
    @Override
    public Page<WalletTransactionResponseDto> getTransactions(String emailId, Pageable pageable) {
        String userId = getUserIdFromEmail(emailId);
        return transactions.findAllByWalletUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Transactional
    @Override
    public WalletTransactionResponseDto adjustAsAdmin(String emailId, WalletAdjustmentRequestDto request) {
        String userId = getUserIdFromEmail(emailId);
        String actorEmail = getCurrentUserPortal();
        WalletTransactionEntity transaction = apply(
                userId,
                request.type(),
                request.amountCoins(),
                request.reason(),
                request.reference(),
                request.idempotencyKey(),
                actorEmail,
                false
        );
        audit.record(
                "WALLET_" + request.type().name(),
                "WALLET",
                userId,
                request.reason() + "; transaction=" + transaction.getId()
        );
        return toResponse(transaction);
    }

    @Transactional
    public WalletTransactionResponseDto debitForBusiness(
            String userId,
            long amountCoins,
            String reason,
            String reference,
            String idempotencyKey
    ) {
        return toResponse(apply(
                userId,
                WalletTransactionType.DEBIT,
                amountCoins,
                reason,
                reference,
                idempotencyKey,
                "system",
                true
        ));
    }

    @Transactional
    public WalletTransactionResponseDto creditForBusiness(
            String userId,
            long amountCoins,
            String reason,
            String reference,
            String idempotencyKey
    ) {
        return toResponse(apply(
                userId,
                WalletTransactionType.CREDIT,
                amountCoins,
                reason,
                reference,
                idempotencyKey,
                "system",
                false
        ));
    }

    private WalletTransactionEntity apply(
            String userId,
            WalletTransactionType type,
            long amountCoins,
            String reason,
            String reference,
            String idempotencyKey,
            String actorEmail,
            boolean requireActive
    ) {
        if (amountCoins <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coin amount must be positive");
        }
        String normalizedReason = reason == null ? null : reason.trim();
        String normalizedIdempotencyKey = idempotencyKey == null ? null : idempotencyKey.trim();
        String normalizedReference = normalize(reference);
        if (normalizedReason == null || normalizedReason.isBlank() || normalizedReason.length() > 300
                || normalizedIdempotencyKey == null || normalizedIdempotencyKey.isBlank()
                || normalizedIdempotencyKey.length() > 100
                || (reference != null && reference.length() > 160)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet transaction details are invalid");
        }

        UserEntity user = userRepo.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mobile user not found"));
        if (requireActive && user.getAccountStatus() != MobileAccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Inactive mobile users cannot transact");
        }

        WalletAccountEntity wallet = wallets.findByUserIdForUpdate(userId)
                .orElseGet(() -> wallets.saveAndFlush(new WalletAccountEntity(user)));
        var existing = transactions.findByWalletUserIdAndIdempotencyKey(userId, normalizedIdempotencyKey);
        if (existing.isPresent()) {
            WalletTransactionEntity previous = existing.get();
            if (previous.getType() != type
                    || previous.getAmountCoins() != amountCoins
                    || !previous.getReason().equals(normalizedReason)
                    || !java.util.Objects.equals(previous.getReference(), normalizedReference)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key was already used for a different wallet operation"
                );
            }
            return previous;
        }

        long nextBalance;
        try {
            nextBalance = type == WalletTransactionType.CREDIT
                    ? Math.addExact(wallet.getBalanceCoins(), amountCoins)
                    : Math.subtractExact(wallet.getBalanceCoins(), amountCoins);
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wallet balance is outside the supported range");
        }
        if (nextBalance < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient coin balance");
        }

        wallet.setBalanceCoins(nextBalance);
        WalletTransactionEntity transaction = new WalletTransactionEntity(
                wallet,
                type,
                amountCoins,
                nextBalance,
                normalizedReason,
                normalizedReference,
                normalizedIdempotencyKey,
                actorEmail == null || actorEmail.isBlank() ? "system" : actorEmail
        );
        return transactions.saveAndFlush(transaction);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private WalletTransactionResponseDto toResponse(WalletTransactionEntity transaction) {
        return new WalletTransactionResponseDto(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmountCoins(),
                transaction.getBalanceAfter(),
                transaction.getReason(),
                transaction.getReference(),
                transaction.getIdempotencyKey(),
                transaction.getCreatedAt()
        );
    }

    public String getCurrentUserPortal() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        return authentication.getName();
    }

    private String getUserIdFromEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ID is required");
        }
        UserEntity user = userRepo.findByEmailIgnoreCase(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App user not found"));
        return user.getId();
    }
}
