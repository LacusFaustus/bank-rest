package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService("test-encryption-key-32-chars-long!");
    }

    @Test
    void encryptAndDecrypt_ShouldWorkCorrectly() {
        // Given
        String originalText = "1234567890123456";

        // When
        String encrypted = encryptionService.encrypt(originalText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(originalText, decrypted);
        assertNotEquals(originalText, encrypted);
    }

    @Test
    void encrypt_NullInput_ShouldReturnNull() {
        assertNull(encryptionService.encrypt(null));
    }

    @Test
    void decrypt_NullInput_ShouldReturnNull() {
        assertNull(encryptionService.decrypt(null));
    }
}
