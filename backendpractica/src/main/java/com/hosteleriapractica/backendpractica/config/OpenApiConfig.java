package com.hosteleriapractica.backendpractica.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApiInfo() {
        final String esquemaJwt = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Hostelería API")
                        .description("API REST para la gestión de mesas, comandas y carta de un restaurante")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(esquemaJwt))
                .schemaRequirement(esquemaJwt, new SecurityScheme()
                        .name(esquemaJwt)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}