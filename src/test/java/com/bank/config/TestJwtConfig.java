package com.bank.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.bank.security.JwtTokenProvider;

@TestConfiguration
public class TestJwtConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(
                "test-jwt-secret-base64-32-chars-long-12345678",
                3600000L
        );
    }
}
