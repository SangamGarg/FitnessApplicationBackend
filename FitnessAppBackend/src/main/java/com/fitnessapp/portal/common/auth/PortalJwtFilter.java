package com.fitnessapp.portal.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitnessapp.portal.common.enums.UserRole;
import com.fitnessapp.portal.dto.ApiErrorResponseDto;
import com.fitnessapp.portal.repository.PortalAccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.fitnessapp.portal.common.PortalConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortalJwtFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final PortalTokenService tokens;
    private final PortalAccountRepository accounts;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Only portal routes and the developer console use portal JWTs. If this
        // filter runs on ordinary app APIs, a valid app JWT is incorrectly rejected
        // because its token_type is "access", not "portal".
        boolean portalOrDevRoute = path.startsWith("/api/portal/");
        boolean publicOrDocsRoute = EXCLUDED_PATHS.contains(path)
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");

        boolean bypass = !portalOrDevRoute || publicOrDocsRoute;

        if (bypass) {
            log.trace("Bypassing portal JWT filter for endpoint: {}", path);
        }
        return bypass;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String header = request.getHeader(AUTH_HEADER);

        if (header == null) {
            log.trace("No Authorization header found for path: {}", path);
            chain.doFilter(request, response);
            return;
        }

        if (!header.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization header does not start with '{}' for path: {}", BEARER_PREFIX, path);
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            log.warn("Bearer token is empty for path: {}", path);
            writeJsonError(response, HttpStatus.UNAUTHORIZED, "Bearer token cannot be empty", path);
            return;
        }

        log.trace("Processing Bearer token for request: {}", path);

        try {
            Claims claims = tokens.parse(token);
            String email = claims.getSubject();
            String roleName = claims.get("role", String.class);
            Integer tokenRoleId = claims.get("roleId", Integer.class);

            if (email == null || email.isBlank() || roleName == null || roleName.isBlank()) {
                log.warn("JWT claims missing mandatory fields [email={}, roleName={}] for path: {}", email, roleName, path);
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Invalid token payload", path);
                return;
            }

            log.trace("Parsed JWT claims: email={}, roleName={}, roleId={}", email, roleName, tokenRoleId);

            UserRole role;
            try {
                role = UserRole.valueOf(roleName.trim());
            } catch (IllegalArgumentException e) {
                log.warn("Unrecognized role '{}' in token claims for user: {}", roleName, email);
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Invalid token role", path);
                return;
            }

            var accountOpt = accounts.findByEmailIgnoreCase(email);
            if (accountOpt.isEmpty()) {
                log.warn("Portal account lookup failed. No account found for email: {}", email);
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Portal account is disabled or no longer valid", path);
                return;
            }

            var account = accountOpt.get();

            if (!account.isEnabled()) {
                log.warn("Portal account [{}] is disabled", email);
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Portal account is disabled or no longer valid", path);
                return;
            }

            if (account.getPasswordHash() == null || account.getPasswordHash().isBlank()) {
                log.warn("Portal account [{}] has no password hash set (incomplete profile/setup)", email);
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Portal account is disabled or no longer valid", path);
                return;
            }

            if (account.getRole() != role) {
                log.warn("Portal account [{}] role mismatch: token role [{}] vs DB role [{}]", email, role, account.getRole());
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Portal account is disabled or no longer valid", path);
                return;
            }

            if (!Objects.equals(tokenRoleId, account.getRoleId())) {
                log.warn("Portal account [{}] roleId mismatch: token roleId [{}] vs DB roleId [{}]", email, tokenRoleId, account.getRoleId());
                writeJsonError(response, HttpStatus.UNAUTHORIZED, "Portal account is disabled or no longer valid", path);
                return;
            }

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
            var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("User [{}] authenticated successfully with role [{}] for path: {}", email, role, path);

        } catch (JwtException ex) {
            log.warn("JWT parsing failed or expired: {} - Reason: {}", ex.getClass().getSimpleName(), ex.getMessage());
            writeJsonError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired portal token", path);
            return;
        }

        chain.doFilter(request, response);
    }

    private void writeJsonError(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponseDto errorPayload = ApiErrorResponseDto.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        OBJECT_MAPPER.writeValue(response.getOutputStream(), errorPayload);
    }
}