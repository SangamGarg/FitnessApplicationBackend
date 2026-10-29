package com.fitnessapp.portal.service;

import com.fitnessapp.utils.portalUtil.PortalUtil;
import com.fitnessapp.portal.common.auth.PortalLoginRateLimiter;
import com.fitnessapp.portal.common.auth.PortalRefreshTokenEntity;
import com.fitnessapp.portal.common.auth.PortalRefreshTokenRepository;
import com.fitnessapp.portal.common.auth.PortalTokenService;
import com.fitnessapp.portal.common.enums.UserRole;
import com.fitnessapp.portal.repository.PortalAccountRepository;
import com.fitnessapp.portal.tableEntity.PortalAccountEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Handles authentication lifecycles including login, refresh token rotation,
 * logout, and initial account setup for portal users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalAuthService {

    public static final long ACCESS_TOKEN_TTL_SECONDS = PortalUtil.EXPIRATION_SECONDS;
    public static final long REFRESH_TOKEN_TTL_SECONDS = 14L * 24 * 60 * 60; // 14 days in seconds

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int REFRESH_TOKEN_BYTE_LENGTH = 64;
    private static final int SETUP_TOKEN_BYTE_LENGTH = 32;

    private final PortalAccountRepository accounts;
    private final PortalRefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwords;
    private final PortalTokenService tokens;
    private final PortalLoginRateLimiter loginRateLimiter;
    private final SecureRandom random = new SecureRandom();

    /**
     * Authenticates a user by email and password, purges expired tokens,
     * invalidates prior sessions, and issues a new access/refresh token pair.
     */
    @Transactional
    public Map<String, Object> login(String email, String password, String clientAddress) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        log.debug("Processing login attempt for email: [{}]", normalizedEmail);
        String rateLimitKey = sha256(normalizedEmail + '\u0000'
                + (clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress));
        loginRateLimiter.assertAllowed(rateLimitKey);

        try {
            // Purge expired tokens across the database
            refreshTokens.deleteByExpiresAtBefore(Instant.now());

            PortalAccountEntity account = accounts.findByEmailIgnoreCase(normalizedEmail)
                    .orElseThrow(() -> {
                        log.warn("Login rejected: Account not found for email [{}]", normalizedEmail);
                        return unauthorized();
                    });

            if (!account.isEnabled()) {
                log.warn("Login rejected: Account [{}] is disabled", normalizedEmail);
                throw unauthorized();
            }

            if (account.getPasswordHash() == null) {
                log.warn("Login rejected: Account [{}] has no password set (setup pending)", normalizedEmail);
                throw unauthorized();
            }

            String rawPassword = password == null ? "" : password;
            if (!passwords.matches(rawPassword, account.getPasswordHash())) {
                log.warn("Login rejected: Password mismatch for email [{}]", normalizedEmail);
                throw unauthorized();
            }

            // Revoke prior active sessions for single-session enforcement
            revokeAll(account);

            String rawRefreshToken = newRefreshToken();
            String tokenHash = sha256(rawRefreshToken);
            Instant expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_TTL_SECONDS);

            refreshTokens.save(new PortalRefreshTokenEntity(account, tokenHash, expiresAt));
            log.info("Portal login successful: accountId={}, role={}", account.getId(), account.getRole());

            Map<String, Object> session = sessionResponse(account, rawRefreshToken);
            loginRateLimiter.recordSuccess(rateLimitKey);
            return session;
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                loginRateLimiter.recordFailure(rateLimitKey);
            }
            throw ex;
        }
    }

    /**
     * Validates an active refresh token, revokes it, and issues a fresh rotated token pair.
     */
    @Transactional
    public Map<String, Object> refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            log.warn("Refresh rejected: Provided refresh token is null or blank");
            throw unauthorizedRefresh();
        }

        String tokenHash = sha256(rawRefreshToken);
        PortalRefreshTokenEntity stored = refreshTokens.findActiveByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh rejected: No active token found matching hash");
                    return unauthorizedRefresh();
                });

        Instant now = Instant.now();
        if (!stored.getExpiresAt().isAfter(now)) {
            log.warn("Refresh rejected: Token expired at [{}] (now: [{}])", stored.getExpiresAt(), now);
            stored.revoke();
            throw unauthorizedRefresh();
        }

        PortalAccountEntity account = stored.getAccount();
        if (!account.isEnabled() || account.getPasswordHash() == null) {
            log.warn("Refresh rejected: Associated account [{}] is disabled or password is unset", account.getEmail());
            stored.revoke();
            throw unauthorizedRefresh();
        }

        // Token rotation: Revoke current refresh token and persist next
        stored.revoke();
        String nextRefreshToken = newRefreshToken();
        String nextTokenHash = sha256(nextRefreshToken);
        Instant nextExpiry = now.plusSeconds(REFRESH_TOKEN_TTL_SECONDS);

        refreshTokens.save(new PortalRefreshTokenEntity(account, nextTokenHash, nextExpiry));
        log.debug("Refresh token rotated successfully for accountId={}", account.getId());

        return sessionResponse(account, nextRefreshToken);
    }

    /**
     * Revokes the active session matching the provided refresh token.
     */
    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            log.trace("Logout skipped: Refresh token was null or blank");
            return;
        }

        String tokenHash = sha256(rawRefreshToken);
        refreshTokens.findActiveByTokenHashForUpdate(tokenHash).ifPresentOrElse(
                token -> {
                    token.revoke();
                    refreshTokens.save(token);
                    log.info("Active portal session revoked for accountId={}", token.getAccount().getId());
                },
                () -> log.debug("Logout called with invalid, revoked, or non-existent refresh token")
        );
    }

    /**
     * Completes initial account onboarding using a single-use setup token.
     */
    @Transactional
    public void completeSetup(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            log.warn("Account setup failed: Missing setup token");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid setup token is required");
        }

        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH || newPassword.length() > MAX_PASSWORD_LENGTH) {
            log.warn("Account setup failed: Password length violates constraints [{}-{}]", MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters"
            );
        }

        String tokenHash = sha256(rawToken);
        PortalAccountEntity account = accounts.findBySetupTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Account setup failed: Token hash not found in database");
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired setup link");
                });

        Instant now = Instant.now();
        if (account.getSetupTokenExpiresAt() == null || !account.getSetupTokenExpiresAt().isAfter(now)) {
            log.warn("Account setup failed: Setup token for account [{}] expired or missing expiry", account.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired setup link");
        }

        account.setPasswordHash(passwords.encode(newPassword));
        account.setSetupTokenHash(null);
        account.setSetupTokenExpiresAt(null);
        accounts.save(account);

        revokeAll(account);
        log.info("Password setup completed and previous sessions cleared for accountId={}", account.getId());
    }

    /**
     * Builds the session payload returned to authentication controllers.
     */
    private Map<String, Object> sessionResponse(PortalAccountEntity account, String rawRefreshToken) {
        UserRole role = account.getRole();
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("accessToken", tokens.create(account));
        result.put("tokenType", PortalUtil.BEARER_PREFIX.trim());
        result.put("expiresIn", ACCESS_TOKEN_TTL_SECONDS);
        result.put("role", role.name());
        result.put("roleId", role.getId());
        result.put("name", account.getFullName());
        result.put("email", account.getEmail());
        // Controller removes this key before sending JSON to client; set via HttpOnly cookie
        result.put("_refreshToken", rawRefreshToken);

        return result;
    }

    /**
     * Invalidates all active refresh tokens for the given account.
     */
    private void revokeAll(PortalAccountEntity account) {
        List<PortalRefreshTokenEntity> activeTokens = refreshTokens.findAllByAccountIdAndRevokedAtIsNull(account.getId());
        if (!activeTokens.isEmpty()) {
            activeTokens.forEach(PortalRefreshTokenEntity::revoke);
            refreshTokens.saveAll(activeTokens);
            log.debug("Revoked {} active refresh token(s) for accountId={}", activeTokens.size(), account.getId());
        }
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTE_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String newSetupToken(SecureRandom random) {
        byte[] bytes = new byte[SETUP_TOKEN_BYTE_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private ResponseStatusException unauthorizedRefresh() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh session is invalid or expired. Please sign in again.");
    }
}