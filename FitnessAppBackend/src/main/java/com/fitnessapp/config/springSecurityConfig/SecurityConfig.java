package com.fitnessapp.config.springSecurityConfig;

import com.fitnessapp.config.springSecurityConfig.jwt.JwtFilter;
import com.fitnessapp.portal.common.auth.PortalJwtFilter;
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

import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_AUTH_ENDPOINT;
import static com.fitnessapp.utils.appUtil.AppUtilEndpoint.APP_OTP_ENDPOINT;
import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_AUTH_BASE_REQUEST_URL;
import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_BASE_URL;

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
                        .requestMatchers(PORTAL_AUTH_BASE_REQUEST_URL + "/login", PORTAL_AUTH_BASE_REQUEST_URL + "/refresh", PORTAL_AUTH_BASE_REQUEST_URL + "/logout", PORTAL_AUTH_BASE_REQUEST_URL + "/setup").permitAll()
                        .requestMatchers(PORTAL_BASE_URL + "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/dev/support/**").hasRole("ADMIN")
                        .requestMatchers(PORTAL_BASE_URL + "/**").hasAnyRole("ADMIN", "DEVELOPER")

                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(APP_AUTH_ENDPOINT + "/loginRegister", APP_AUTH_ENDPOINT + "/refresh-token", APP_OTP_ENDPOINT + "/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/health").permitAll()
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
