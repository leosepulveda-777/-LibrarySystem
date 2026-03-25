package com.library.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / SpringDoc OpenAPI
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Library System API",
        version = "1.0.0",
        description = "Sistema completo de gestión de biblioteca con autenticación JWT, " +
                      "catálogo, préstamos, reservas, multas y reportes.",
        contact = @Contact(name = "Library System", email = "admin@library.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor de desarrollo")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer token. Obtén el token en /api/v1/auth/login y úsalo aquí.",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
