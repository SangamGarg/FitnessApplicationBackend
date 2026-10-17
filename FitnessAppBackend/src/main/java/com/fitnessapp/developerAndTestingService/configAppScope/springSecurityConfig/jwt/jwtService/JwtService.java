package com.fitnessapp.developerAndTestingService.configAppScope.springSecurityConfig.jwt.jwtService;

import com.fitnessapp.developerAndTestingService.configAppScope.springSecurityConfig.jwt.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtConfig jwtConfig;
    private final SecretKey signingKey;
    private final JwtParser parser;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        // Built once. Fails fast at startup if JWT_SECRET_KEY is missing, not Base64, or shorter than 256 bits.
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecretKey()));
        this.parser = Jwts.parser().verifyWith(signingKey).build();
    }

    // ---------- creation ----------

    public String generateToken(String id, String email) {
        return buildToken(id, email, TYPE_ACCESS, jwtConfig.getExpirationMs());
    }

    public String generateRefreshToken(String id, String email) {
        return buildToken(id, email, TYPE_REFRESH, jwtConfig.getRefreshExpirationMs());
    }

    private String buildToken(String id, String email, String type, long ttlMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(id)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TOKEN_TYPE, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMs))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // ---------- validation ----------

    /**
     * Valid ACCESS token for this user. A refresh token is rejected here.
     */
    public boolean validToken(String token, UserDetails userDetails) {
        return isValid(token, userDetails, TYPE_ACCESS);
    }

    /**
     * Valid REFRESH token for this user. An access token is rejected here.
     */
    public boolean isRefreshTokenValid(String refreshToken, UserDetails userDetails) {
        return isValid(refreshToken, userDetails, TYPE_REFRESH);
    }

    private boolean isValid(String token, UserDetails userDetails, String expectedType) {
        try {
            // parseSignedClaims verifies the signature and throws ExpiredJwtException if expired.
            Claims claims = extractAllClaims(token);
            return expectedType.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))
                    && claims.getSubject() != null
                    && claims.getSubject().equals(userDetails.getUsername());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Not valid {} token: {}", expectedType, e.getLocalizedMessage());
            return false;
        }
    }

    // ---------- extraction (throw JwtException when the token is invalid or expired) ----------

    public String extractId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get(CLAIM_EMAIL, String.class);
    }

    private Claims extractAllClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }
}