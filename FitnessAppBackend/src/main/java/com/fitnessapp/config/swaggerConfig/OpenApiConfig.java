package com.fitnessapp.config.swaggerConfig;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Fitness App Backend API").version("1.0.0")
                .description("Fitness App REST API. Portal and developer endpoints use Bearer JWT authentication."))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
    @Bean public OpenApiCustomizer authenticationCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
            boolean publicEndpoint = path.equals("/api/portal/auth/login") || path.equals("/api/portal/auth/refresh")
                    || path.equals("/api/portal/auth/logout") || path.equals("/api/portal/auth/setup")
                    || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui");
            operation.setSecurity(publicEndpoint ? List.of() : List.of(new SecurityRequirement().addList("bearerAuth")));
        }));
    }
}
