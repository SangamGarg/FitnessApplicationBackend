package com.fitnessapp.config.springSecurityConfig;

import com.fitnessapp.config.exceptionConfig.ApiErrorResponseDto;
import com.fitnessapp.config.springSecurityConfig.jwt.JwtFilter;
import com.fitnessapp.portal.portalAuth.tokenJwt.PortalJwtFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_AUTH_ENDPOINT;
import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_OTP_ENDPOINT;
import static com.fitnessapp.utils.portalUtil.PortalUtil.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public FilterRegistrationBean<PortalJwtFilter> portalJwtFilterServletRegistration(PortalJwtFilter filter) {
        FilterRegistrationBean<PortalJwtFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            PortalJwtFilter portalJwtFilter,
            ObjectMapper objectMapper
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PORTAL_AUTH_ENDPOINT + "/login", PORTAL_AUTH_ENDPOINT + "/refresh", PORTAL_AUTH_ENDPOINT + "/logout", PORTAL_AUTH_ENDPOINT + "/setup", PORTAL_OVERVIEW_ENDPOINT + "/**").permitAll()
                        .requestMatchers(PORTAL_BASE_ENDPOINT + "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/dev/support/**").hasRole("ADMIN")
                        .requestMatchers(PORTAL_BASE_ENDPOINT + "/**").hasAnyRole("ADMIN", "DEVELOPER")

                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(APP_AUTH_ENDPOINT + "/loginRegister", APP_AUTH_ENDPOINT + "/refresh-token", APP_OTP_ENDPOINT + "/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(APP_AUTH_ENDPOINT + "/logout").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(portalJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(accessDeniedHandler(objectMapper)));
        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeSecurityError(
                objectMapper,
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
        );
    }

    private AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeSecurityError(
                objectMapper,
                request,
                response,
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource"
        );
    }

    private void writeSecurityError(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ApiErrorResponseDto.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
