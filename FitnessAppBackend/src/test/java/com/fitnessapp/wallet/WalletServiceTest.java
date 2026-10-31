package com.fitnessapp.wallet;

import com.fitnessapp.portal.audit.PortalAuditService;
import com.fitnessapp.appAuth.model.enums.MobileAccountStatus;
import com.fitnessapp.appAuth.model.entity.UserEntity;
import com.fitnessapp.appAuth.repo.UserRepo;
import com.fitnessapp.wallet.dto.WalletAdjustmentRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletServiceTest {

    private UserRepo users;
    private WalletAccountRepository wallets;
    private WalletTransactionRepository transactions;
    private PortalAuditService audit;
    private WalletService service;
    private UserEntity user;
    private WalletAccountEntity wallet;

    @BeforeEach
    void setUp() {
        users = mock(UserRepo.class);
        wallets = mock(WalletAccountRepository.class);
        transactions = mock(WalletTransactionRepository.class);
        audit = mock(PortalAuditService.class);
        service = new WalletService(users, wallets, transactions, audit);
        user = new UserEntity("user-1", "user@example.test", MobileAccountStatus.ACTIVE, null);
        wallet = new WalletAccountEntity(user);
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(wallets.findByUserIdForUpdate(user.getId())).thenReturn(Optional.of(wallet));
        when(transactions.findByWalletUserIdAndIdempotencyKey(user.getId(), "adjustment-1"))
                .thenReturn(Optional.empty());
        when(transactions.saveAndFlush(any(WalletTransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void creditRecordsLedgerAndAudit() {
        var result = service.adjustAsAdmin(
                user.getId(),
                "admin@example.test",
                new WalletAdjustmentRequestDto(
                        WalletTransactionType.CREDIT, 25, "Promotional grant", "campaign-7", "adjustment-1"
                )
        );

        assertEquals(25, wallet.getBalanceCoins());
        assertEquals(WalletTransactionType.CREDIT, result.type());
        assertEquals(25, result.balanceAfter());
        verify(transactions).saveAndFlush(any(WalletTransactionEntity.class));
        verify(audit).record("WALLET_CREDIT", "WALLET", user.getId(), "Promotional grant; transaction=null");
    }

    @Test
    void replayingTheSameIdempotencyKeyDoesNotApplyTheAdjustmentAgain() {
        WalletTransactionEntity prior = new WalletTransactionEntity(
                wallet,
                WalletTransactionType.CREDIT,
                25,
                25,
                "Promotional grant",
                "campaign-7",
                "adjustment-1",
                "admin@example.test"
        );
        when(transactions.findByWalletUserIdAndIdempotencyKey(user.getId(), "adjustment-1"))
                .thenReturn(Optional.of(prior));

        var result = service.adjustAsAdmin(
                user.getId(),
                "admin@example.test",
                new WalletAdjustmentRequestDto(
                        WalletTransactionType.CREDIT, 25, "Promotional grant", "campaign-7", "adjustment-1"
                )
        );

        assertEquals(0, wallet.getBalanceCoins());
        assertEquals(25, result.balanceAfter());
        verify(transactions, never()).saveAndFlush(any(WalletTransactionEntity.class));
    }

    @Test
    void debitCannotMakeWalletBalanceNegative() {
        wallet.setBalanceCoins(10);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.adjustAsAdmin(
                        user.getId(),
                        "admin@example.test",
                        new WalletAdjustmentRequestDto(
                                WalletTransactionType.DEBIT, 11, "Purchase", null, "adjustment-1"
                        )
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(10, wallet.getBalanceCoins());
        verify(transactions, never()).saveAndFlush(any(WalletTransactionEntity.class));
    }

    @Test
    void idempotencyKeyCannotBeReusedWithDifferentOperation() {
        WalletTransactionEntity prior = new WalletTransactionEntity(
                wallet,
                WalletTransactionType.CREDIT,
                25,
                25,
                "Promotional grant",
                "campaign-7",
                "adjustment-1",
                "admin@example.test"
        );
        when(transactions.findByWalletUserIdAndIdempotencyKey(user.getId(), "adjustment-1"))
                .thenReturn(Optional.of(prior));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.adjustAsAdmin(
                        user.getId(),
                        "admin@example.test",
                        new WalletAdjustmentRequestDto(
                                WalletTransactionType.CREDIT, 30, "Promotional grant", "campaign-7", "adjustment-1"
                        )
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
