package com.bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Development Server");

        Contact contact = new Contact()
                .name("Bank API Support")
                .email("api-support@bank.com")
                .url("https://bank.com/support");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Bank Card Management API")
                .version("1.0.0")
                .contact(contact)
                .description("""
                This is a secure REST API for managing bank cards with JWT authentication.
                
                ## Features:
                - 🔐 JWT-based authentication
                - 💳 Card management (create, update, delete)
                - 💸 Money transfers between cards
                - 👥 Role-based access control (User/Admin)
                - 📊 Real-time monitoring
                - 🛡️ Security audit logging
                
                ## Authentication:
                1. Get JWT token from `/api/v1/auth/login`
                2. Include token in header: `Authorization: Bearer <token>`
                """)
                .license(license);

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token without 'Bearer' prefix");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .addSecurityItem(securityRequirement)
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme));
    }
}
