package com.fitnessapp.portal.service;

import com.fitnessapp.portal.common.auth.PortalLoginRateLimiter;
import com.fitnessapp.portal.common.auth.PortalRefreshTokenRepository;
import com.fitnessapp.portal.common.auth.PortalTokenService;
import com.fitnessapp.portal.common.enums.UserRole;
import com.fitnessapp.portal.repository.PortalAccountRepository;
import com.fitnessapp.portal.tableEntity.PortalAccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalAuthServiceTest {

    private static final String EMAIL = "admin@example.test";
    private static final String CLIENT_ADDRESS = "192.0.2.10";
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Mock
    private PortalAccountRepository accounts;

    @Mock
    private PortalRefreshTokenRepository refreshTokens;

    @Mock
    private PasswordEncoder passwords;

    @Mock
    private PortalTokenService tokens;

    private PortalAuthService service;

    @BeforeEach
    void setUp() {
        service = new PortalAuthService(
                accounts,
                refreshTokens,
                passwords,
                tokens,
                new PortalLoginRateLimiter()
        );
    }

    @Test
    void repeatedInvalidCredentialsAreThrottledBeforeAccountLookup() {
        PortalAccountEntity account = new PortalAccountEntity();
        account.setId(1L);
        account.setEmail(EMAIL);
        account.setFullName("Portal Admin");
        account.setRole(UserRole.ADMIN);
        account.setPasswordHash("encoded-password");
        when(accounts.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(account));
        when(passwords.matches(anyString(), anyString())).thenReturn(false);

        for (int i = 0; i < MAX_FAILED_ATTEMPTS; i++) {
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.login(EMAIL, "wrong-password", CLIENT_ADDRESS)
            );
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }

        ResponseStatusException throttled = assertThrows(
                ResponseStatusException.class,
                () -> service.login(EMAIL, "wrong-password", CLIENT_ADDRESS)
        );
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, throttled.getStatusCode());
        verify(accounts, times(MAX_FAILED_ATTEMPTS)).findByEmailIgnoreCase(EMAIL);
    }

    @Test
    void passwordSetupConsumesOneTimeToken() {
        String rawToken = "one-time-setup-token";
        String tokenHash = PortalAuthService.sha256(rawToken);
        String newPassword = "new-secure-password";
        PortalAccountEntity account = new PortalAccountEntity();
        account.setId(1L);
        account.setSetupTokenHash(tokenHash);
        account.setSetupTokenExpiresAt(Instant.now().plusSeconds(3600));
        when(accounts.findBySetupTokenHash(tokenHash)).thenReturn(Optional.of(account), Optional.empty());
        when(passwords.encode(newPassword)).thenReturn("encoded-password");
        when(refreshTokens.findAllByAccountIdAndRevokedAtIsNull(1L)).thenReturn(List.of());

        service.completeSetup(rawToken, newPassword);

        assertEquals("encoded-password", account.getPasswordHash());
        assertNull(account.getSetupTokenHash());
        assertNull(account.getSetupTokenExpiresAt());
        verify(accounts).save(account);

        ResponseStatusException reused = assertThrows(
                ResponseStatusException.class,
                () -> service.completeSetup(rawToken, newPassword)
        );
        assertEquals(HttpStatus.BAD_REQUEST, reused.getStatusCode());
    }

    @Test
    void expiredPasswordSetupTokenIsRejected() {
        String rawToken = "expired-setup-token";
        String tokenHash = PortalAuthService.sha256(rawToken);
        PortalAccountEntity account = new PortalAccountEntity();
        account.setSetupTokenHash(tokenHash);
        account.setSetupTokenExpiresAt(Instant.now().minusSeconds(1));
        when(accounts.findBySetupTokenHash(tokenHash)).thenReturn(Optional.of(account));

        ResponseStatusException expired = assertThrows(
                ResponseStatusException.class,
                () -> service.completeSetup(rawToken, "new-secure-password")
        );

        assertEquals(HttpStatus.BAD_REQUEST, expired.getStatusCode());
    }
}
