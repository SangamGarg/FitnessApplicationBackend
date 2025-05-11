package com.fitnessapp.userDetailsAndAuthService.config.springSecurityConfig;

import com.fitnessapp.DeveloperAndTestingService.devAndTestingServiceUtilities.DevAndTestingServiceUtility;
import com.fitnessapp.userDetailsAndAuthService.userDetailsAndAuthServiceUtilities.AppConstantsUserAndAuthService;
import com.fitnessapp.userDetailsAndAuthService.config.springSecurityConfig.jwt.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Autowired
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                //disable csrf token
                .csrf(AbstractHttpConfigurer::disable)
                //authenticate user
                .authorizeHttpRequests(request ->
                        request
                                //→ Allows anyone (even unauthenticated users) to access the /login and /register endpoints.
                                .requestMatchers(
                                        AppConstantsUserAndAuthService.API_PREFIX + "/loginRegister",
                                        "/api/v1/dev/**",
                                        "/**"
                                )
                                .permitAll()
                                .anyRequest()
                                //All other endpoints will require a valid JWT (or other auth method) to access.
                                .authenticated())

                //make http stateless
                .sessionManagement(session -> session.sessionCreationPolicy
                        (SessionCreationPolicy.STATELESS))

                //below to validate the jwt token
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling ->
                        handling.authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")));

        return httpSecurity.build();
    }

    //JWT Implementation
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
}