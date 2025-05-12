package com.fitnessapp.userDetailsAndAuthService.services.jwtService;

import com.fitnessapp.userDetailsAndAuthService.config.springSecurityConfig.jwt.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
//import org.springframework.security.core.userdetails.UserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    private final JwtConfig jwtConfig;


    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }


    public String generateToken(String id, String email) {
        return Jwts.builder()
                .subject(id)
                .claim("email", email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .signWith(getJwtKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String id, String email) {
        return Jwts.builder()
                .subject(id)
                .claim("email", email)
//                .claim("token_type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshExpirationMs()))
                .signWith(getJwtKey(), Jwts.SIG.HS256)
                .compact();
    }

//    public boolean isRefreshTokenValid(String token) {
//        try {
//            return "refresh".equals(extractAllClaims(token).get("token_type"));
//        } catch (JwtException e) {
//            return false;
//        }
//    }

    public boolean isRefreshTokenValid(String refreshToken, UserDetails userDetails) {
        try {
            final String id = extractId(refreshToken);
            return (id.equals(userDetails.getUsername()) && !isTokenExpired(refreshToken));
        } catch (JwtException e) {
            return false;
        }
    }


    private SecretKey getJwtKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getJwtKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validToken(String token, UserDetails userDetails) {
        try {
            final String id = extractId(token);
            return (id.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            log.error("Not Valid Token{}", e.getLocalizedMessage());
            return false;
        }
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }


    private void generateNewJwtSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey1 = keyGen.generateKey();
            String secretKey = Base64.getEncoder().encodeToString(secretKey1.getEncoded());
            System.out.println("SCRETE KEY :    " + secretKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}