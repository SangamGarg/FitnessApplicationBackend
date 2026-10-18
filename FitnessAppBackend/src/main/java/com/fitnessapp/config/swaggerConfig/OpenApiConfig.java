
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

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fitness App Backend API")
                        .version("1.0.0")
                        .description("Fitness App REST API"))
                .components(new Components()
                        // JWT authentication
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))

                        // Developer API name header
                        .addSecuritySchemes("nameHeader",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-NAME"))

                        // Developer API key header
                        .addSecuritySchemes("apiKeyHeader",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-KEY")));
    }

    @Bean
    public OpenApiCustomizer authenticationCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {

            pathItem.readOperations().forEach(operation -> {

                // Developer/testing endpoints
                if (path.startsWith("/api/v1/dev/")) {

                    // Both headers are required together
                    operation.setSecurity(List.of(
                            new SecurityRequirement()
                                    .addList("nameHeader")
                                    .addList("apiKeyHeader")
                    ));

                } else {

                    // All other endpoints use JWT
                    operation.setSecurity(List.of(
                            new SecurityRequirement()
                                    .addList("bearerAuth")
                    ));
                }
            });
        });
    }
}
