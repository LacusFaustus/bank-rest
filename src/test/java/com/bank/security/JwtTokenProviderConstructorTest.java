package com.bank.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderConstructorTest {

    private final long validity = 3600000L;

    @Test
    void constructor_withValidBase64Key_shouldWork() {
        // Arrange - создаем валидный ключ 32 байта в Base64
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) (i + 1);
        }
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        // Act
        JwtTokenProvider provider = new JwtTokenProvider(base64Key, validity);

        // Assert
        assertNotNull(provider);
    }

    @Test
    void constructor_withNonBase64ValidKey_shouldConvertAndWork() {
        // Arrange - строка длиной 32 символа (будет преобразована в Base64)
        String nonBase64Key = "valid-32-char-long-secret-key-here!";

        // Act
        JwtTokenProvider provider = new JwtTokenProvider(nonBase64Key, validity);

        // Assert
        assertNotNull(provider);
    }
}
