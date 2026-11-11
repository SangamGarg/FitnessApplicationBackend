
package com.fitnessapp.config.springSecurityConfig.jwt;

import com.fitnessapp.config.springSecurityConfig.jwt.jwtService.JwtTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_AUTH_ENDPOINT;
import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_OTP_ENDPOINT;
import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_BASE_ENDPOINT;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals(APP_AUTH_ENDPOINT + "/loginRegister")
                || path.equals(APP_AUTH_ENDPOINT + "/refresh-token")
                || path.startsWith(APP_OTP_ENDPOINT)
                || path.startsWith(PORTAL_BASE_ENDPOINT + "/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        //TODO HAVE TO REMOVE
        System.out.println(authHeader);
        String token = authHeader.substring(7).trim();

        try {
            String id = jwtTokenService.extractId(token);

            if (id == null || id.isBlank()) {
                sendUnauthorized(response, "Invalid JWT");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(id);

                if (!jwtTokenService.validToken(token, userDetails)) {
                    sendUnauthorized(response, "Invalid JWT");
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
// makes every later log line of this request carry the user id (cleared by RequestLoggingFilter)
                MDC.put("userId", userDetails.getUsername());
            }

        } catch (ExpiredJwtException ex) {
            log.debug("JWT expired: {}", ex.getMessage());
            sendUnauthorized(response, "Token expired");
            return;

        } catch (JwtException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            sendUnauthorized(response, "Invalid JWT");
            return;

        } catch (UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "User not found");
            return;

        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            sendUnauthorized(response, "Authentication failed");
            return;
        }

        // Keep outside the try-catch so application exceptions
        // are not converted into JWT authentication errors.
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        SecurityContextHolder.clearContext();
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                message
        );
    }
}
