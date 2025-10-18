package com.bank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private final String encryptionKey;

    // Конструктор для тестов
    public EncryptionService() {
        this.encryptionKey = "test-encryption-key-32-chars-long!";
    }

    public EncryptionService(@Value("${app.encryption.key:bank-encryption-key-32-chars-long!}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private SecretKeySpec getSecretKey() {
        // Обеспечиваем ключ длиной 16, 24 или 32 байта
        byte[] keyBytes = new byte[32];
        byte[] originalBytes = encryptionKey.getBytes();
        System.arraycopy(originalBytes, 0, keyBytes, 0, Math.min(originalBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public String encrypt(String data) {
        try {
            if (data == null) return null;

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null) return null;

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}
