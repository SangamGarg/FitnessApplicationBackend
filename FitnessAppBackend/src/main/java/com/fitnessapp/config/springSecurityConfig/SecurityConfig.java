package com.fitnessapp.config.springSecurityConfig;

import com.fitnessapp.config.springSecurityConfig.jwt.JwtFilter;
import com.fitnessapp.portal.common.auth.PortalJwtFilter;
import com.fitnessapp.appAuth.utils.AppAuthUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.fitnessapp.utils.appUtil.AppUtil.APP_AUTH_ENDPOINT;
import static com.fitnessapp.utils.appUtil.AppUtil.APP_OTP_ENDPOINT;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PortalJwtFilter portalJwtFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/portal/auth/login", "/api/portal/auth/refresh", "/api/portal/auth/logout", "/api/portal/auth/setup").permitAll()
                        .requestMatchers("/api/portal/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/dev/support/**").hasRole("ADMIN")
                        .requestMatchers("/api/portal/**").hasAnyRole("ADMIN", "DEVELOPER")
                        .requestMatchers(APP_AUTH_ENDPOINT + "/loginRegister", APP_AUTH_ENDPOINT + "/refresh-token", APP_AUTH_ENDPOINT + "/otp/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/health", APP_OTP_ENDPOINT + "/**").permitAll()
                        .requestMatchers(APP_AUTH_ENDPOINT + "/logout").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(portalJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
