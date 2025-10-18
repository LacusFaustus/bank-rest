package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
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
        // When
        String result = encryptionService.encrypt(null);

        // Then
        assertNull(result);
    }

    @Test
    void decrypt_NullInput_ShouldReturnNull() {
        // When
        String result = encryptionService.decrypt(null);

        // Then
        assertNull(result);
    }

    @Test
    void encrypt_EmptyString_ShouldWork() {
        // Given
        String originalText = "";

        // When
        String encrypted = encryptionService.encrypt(originalText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertEquals(originalText, decrypted);
    }
}
