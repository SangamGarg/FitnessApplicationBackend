package com.fitnessapp.portal.controller;

import com.fitnessapp.portal.common.PortalUtils;
import com.fitnessapp.portal.dto.LoginRequestDto;
import com.fitnessapp.portal.dto.SetupRequestDto;
import com.fitnessapp.portal.service.PortalAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PortalUtils.PORTAL_AUTH_BASE_REQUEST_URL)
@Tag(name = "Portal Authentication", description = "Admin and developer portal login, onboarding and session refresh")
public class PortalAuthController {

    private static final String REFRESH_COOKIE = "portalRefreshToken";
    private static final String COOKIE_PATH = "/api/portal/auth";

    private final PortalAuthService auth;

    @PostMapping("/login")
    @Operation(summary = "Log in to the portal", description = "Returns a short-lived access JWT and stores the refresh token in an HttpOnly cookie.")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        log.info("REST request to log in portal user: [{}]", request.email());
        Map<String, Object> session = auth.login(request.email(), request.password());
        log.info("User [{}] authenticated successfully", request.email());
        return sessionResponse(session, response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh portal access token", description = "Rotates the HttpOnly refresh token and issues a new 30-minute access JWT.")
    public ResponseEntity<Map<String, Object>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.debug("REST request to refresh portal session");
        String rawRefreshToken = readRefreshCookie(request);
        Map<String, Object> session = auth.refresh(rawRefreshToken);
        log.debug("Portal session refreshed successfully");
        return sessionResponse(session, response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out of the portal", description = "Revokes the current refresh token and clears its cookie.")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("REST request to sign out from portal");
        String rawRefreshToken = readRefreshCookie(request);
        auth.logout(rawRefreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie("", 0).toString());
        log.info("Portal session terminated and refresh cookie cleared");
        return ResponseEntity.ok(Map.of("message", "Signed out successfully."));
    }

    @PostMapping("/setup")
    @Operation(summary = "Set portal password", description = "Consumes a one-time, expiring email token to set the account password.")
    public ResponseEntity<Map<String, String>> setup(@Valid @RequestBody SetupRequestDto request) {
        log.info("REST request to set portal password using setup token");
        auth.completeSetup(request.token(), request.password());
        log.info("Password setup completed successfully");
        return ResponseEntity.ok(Map.of("message", "Password set. You can now sign in."));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current portal identity")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated request to /me endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .orElse("UNKNOWN");

        log.trace("Identity fetched for user: [{}], role: [{}]", email, role);
        return ResponseEntity.ok(Map.of("email", email, "role", role));
    }

    private ResponseEntity<Map<String, Object>> sessionResponse(
            Map<String, Object> session,
            HttpServletResponse response
    ) {
        Map<String, Object> body = new LinkedHashMap<>(session);
        Object refresh = body.remove("_refreshToken");

        String refreshTokenValue = refresh instanceof String ? (String) refresh : "";
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie(refreshTokenValue, PortalAuthService.REFRESH_TOKEN_TTL_SECONDS).toString()
        );

        return ResponseEntity.ok(body);
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            log.trace("No cookies found on request");
            return null;
        }

        return Arrays.stream(cookies)
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private ResponseCookie refreshCookie(String value, long maxAgeSeconds) {
        boolean secure = Boolean.parseBoolean(
                System.getProperty("portal.auth.cookie-secure",
                        System.getenv().getOrDefault("PORTAL_AUTH_COOKIE_SECURE", "false"))
        );

        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }
}