package com.fitnessapp.config.springSecurityConfig.jwt;

import com.fitnessapp.config.springSecurityConfig.jwt.jwtService.JwtTokenService;
import com.fitnessapp.appAuth.utils.AppAuthUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.fitnessapp.utils.appUtil.AppUtil.APP_AUTH_ENDPOINT;
import static com.fitnessapp.utils.appUtil.AppUtil.APP_OTP_ENDPOINT;
import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_BASE_URL;

@Component
@RequiredArgsConstructor
public class JwtFilter
        extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    /**
     * The login/register and refresh endpoints are public. A client may still send an old (expired)
     * Authorization header to them, so the filter must not look at the header on those paths.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // This filter validates the app/mobile access JWT only. Portal endpoints and
        // developer-console endpoints use PortalJwtFilter and portal-token claims.
        return path.equals(APP_AUTH_ENDPOINT + "/loginRegister")
                || path.equals(APP_AUTH_ENDPOINT + "/refresh-token")
                || path.startsWith(APP_OTP_ENDPOINT)
                || path.startsWith("/api/v1/dev/support/")
                || path.startsWith(PORTAL_BASE_URL + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = authHeader.substring(7);
            String id = jwtTokenService.extractId(token);
            if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(id);
                // validToken only accepts ACCESS tokens, so a refresh token cannot be used here.
                if (jwtTokenService.validToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    // makes every later log line of this request carry the user id (cleared by RequestLoggingFilter)
                    MDC.put("userId", userDetails.getUsername());
                }
            }
        } catch (ExpiredJwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
