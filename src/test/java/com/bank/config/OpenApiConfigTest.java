package com.bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void customOpenAPI_shouldReturnConfiguredOpenAPI() {
        // Act
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getServers());
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getSecurity());

        // Проверка информации
        Info info = openAPI.getInfo();
        assertEquals("Bank Card Management API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertNotNull(info.getContact());
        assertNotNull(info.getLicense());

        // Проверка серверов
        assertEquals(1, openAPI.getServers().size());
        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost:8080", server.getUrl());
        assertEquals("Development Server", server.getDescription());

        // Проверка безопасности
        assertEquals(1, openAPI.getSecurity().size());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication"));
    }

    @Test
    void customOpenAPI_shouldContainJwtSecurityScheme() {
        // Act
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication"));

        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertEquals("http", securityScheme.getType().toString().toLowerCase());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }
}
