package com.fitnessapp.developerAndTestingService.configAppScope.springSecurityConfig.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {

    private String secretKey;
    private long expirationMs;
    private long refreshExpirationMs;

}
