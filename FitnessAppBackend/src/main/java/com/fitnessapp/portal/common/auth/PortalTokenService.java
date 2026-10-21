package com.fitnessapp.portal.common.auth;

import com.fitnessapp.config.springSecurityConfig.jwt.JwtConfig;
import com.fitnessapp.portal.tableEntity.PortalAccountEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.fitnessapp.portal.common.PortalConstants.*;

@Slf4j
@Service
public class PortalTokenService {

    private final SecretKey key;
    private final JwtParser jwtParser;

    public PortalTokenService(JwtConfig config) {
        Objects.requireNonNull(config, "JwtConfig must not be null");
        String secretKey = config.getSecretKey();

        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            int length = secretKey == null ? 0 : secretKey.getBytes(StandardCharsets.UTF_8).length;
            log.error("JWT initialization aborted: secret key is shorter than 32 bytes ({} bytes provided)", length);
            throw new IllegalStateException("JWT_SECRET_KEY must be at least 32 bytes for HS256");
        }

        byte[] secret = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secret);

        // Build and cache parser instance once rather than rebuilding on every request
        this.jwtParser = Jwts.parser()
                .verifyWith(this.key)
                .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                .build();

        log.info("PortalTokenService initialized with HS256 signing and clock skew tolerance of {}s", CLOCK_SKEW_SECONDS);
    }

    /**
     * Generates a signed portal JWT valid for the configured duration.
     *
     * @param account Portal account entity containing user identity and role
     * @return Signed compact JWT string
     */
    public String create(PortalAccountEntity account) {
        Objects.requireNonNull(account, "PortalAccountEntity must not be null");
        Objects.requireNonNull(account.getEmail(), "Account email must not be null");
        Objects.requireNonNull(account.getRole(), "Account role must not be null");

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(EXPIRATION_SECONDS);
        String subjectEmail = account.getEmail().trim().toLowerCase(Locale.ROOT);

        log.debug("Creating portal JWT for user: [{}], role: [{}], expires at: [{}]", subjectEmail, account.getRole(), expiry);

        String token = Jwts.builder()
                .subject(subjectEmail)
                .claim(CLAIM_TOKEN_TYPE, EXPECTED_TOKEN_TYPE)
                .claim(CLAIM_ROLE, account.getRole().name())
                .claim(CLAIM_ROLE_ID, account.getRoleId())
                .claim(CLAIM_NAME, account.getFullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        log.trace("Generated JWT compact token for user: [{}]", subjectEmail);
        return token;
    }

    /**
     * Validates and parses a signed portal JWT.
     *
     * @param token Compact JWT string
     * @return Extracted token claims
     * @throws JwtException if token is blank, expired, malformed, tampered with, or not a portal token
     */
    public Claims parse(String token) throws JwtException {
        if (token == null || token.isBlank()) {
            log.warn("Attempted to parse a null or blank JWT token");
            throw new JwtException("Token string cannot be empty");
        }

        try {
            Claims claims = jwtParser.parseSignedClaims(token.trim()).getPayload();
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);

            if (!EXPECTED_TOKEN_TYPE.equals(tokenType)) {
                log.warn("Rejected JWT token: Expected token_type '{}', found '{}'", EXPECTED_TOKEN_TYPE, tokenType);
                throw new JwtException("Token is not a portal token");
            }

            log.trace("Successfully parsed and verified portal JWT for subject: [{}]", claims.getSubject());
            return claims;

        } catch (JwtException ex) {
            log.debug("Failed to verify portal JWT: {}", ex.getMessage());
            throw ex;
        }
    }
}