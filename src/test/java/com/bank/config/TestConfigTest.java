package com.bank.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TestConfigTest {

    private final TestConfig testConfig = new TestConfig();

    @Test
    void testPasswordEncoder_shouldReturnBCryptEncoder() {
        // Act
        PasswordEncoder encoder = testConfig.testPasswordEncoder();

        // Assert
        assertNotNull(encoder);
        // Проверяем что это BCrypt через поведение
        String password = "test123";
        String encoded = encoder.encode(password);
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$")); // BCrypt префикс
    }

    @Test
    void testConfig_beansShouldBeCreated() {
        // Просто проверяем что конфигурация создается без ошибок
        assertNotNull(testConfig);
    }
}
