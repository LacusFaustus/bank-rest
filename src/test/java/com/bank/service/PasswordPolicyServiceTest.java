package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyServiceTest {

    private PasswordPolicyService passwordPolicyService;

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyService();
    }

    @Test
    void validatePassword_StrongPassword_ShouldReturnTrue() {
        // Given
        String strongPassword = "StrongPass123!"; // Все 4 категории

        // When
        boolean isValid = passwordPolicyService.validatePassword(strongPassword);

        // Then
        assertTrue(isValid, "Strong password with all categories should be valid");
    }

    @Test
    void validatePassword_TooShort_ShouldReturnFalse() {
        // Given
        String shortPassword = "Short1!"; // 7 символов

        // When
        boolean isValid = passwordPolicyService.validatePassword(shortPassword);

        // Then
        assertFalse(isValid, "Password shorter than 8 characters should be invalid");
    }

    @Test
    void validatePassword_NullPassword_ShouldReturnFalse() {
        // When
        boolean isValid = passwordPolicyService.validatePassword(null);

        // Then
        assertFalse(isValid, "Null password should be invalid");
    }

    @Test
    void validatePassword_NoUppercase_ShouldReturnFalse() {
        // Given
        String noUppercase = "lowercase123!"; // Нет верхнего регистра

        // When
        boolean isValid = passwordPolicyService.validatePassword(noUppercase);

        // Then
        assertFalse(isValid, "Password without uppercase should be invalid");
    }

    @Test
    void validatePassword_OnlyUppercase_ShouldReturnFalse() {
        // Given
        String onlyUppercase = "UPPERCASE"; // Только верхний регистр

        // When
        boolean isValid = passwordPolicyService.validatePassword(onlyUppercase);

        // Then
        assertFalse(isValid, "Password with only uppercase should be invalid (needs 2 additional categories)");
    }

    @Test
    void validatePassword_UpperCaseLowerCaseDigits_ShouldReturnTrue() {
        // Given
        String password = "Password123"; // Верхний + нижний + цифры (3 категории)

        // When
        boolean isValid = passwordPolicyService.validatePassword(password);

        // Then
        assertTrue(isValid, "Password with uppercase, lowercase and digits should be valid");
    }

    @Test
    void validatePassword_UpperCaseLowerCaseSpecial_ShouldReturnTrue() {
        // Given
        String password = "Password!"; // Верхний + нижний + спецсимволы (3 категории)

        // When
        boolean isValid = passwordPolicyService.validatePassword(password);

        // Then
        assertTrue(isValid, "Password with uppercase, lowercase and special chars should be valid");
    }

    @Test
    void validatePassword_UpperCaseDigitsSpecial_ShouldReturnTrue() {
        // Given
        String password = "PASSWORD123!"; // Верхний + цифры + спецсимволы (3 категории)

        // When
        boolean isValid = passwordPolicyService.validatePassword(password);

        // Then
        assertTrue(isValid, "Password with uppercase, digits and special chars should be valid");
    }

    @Test
    void validatePassword_UpperCaseLowerCaseOnly_ShouldReturnFalse() {
        // Given
        String password = "Password"; // Только верхний + нижний (2 категории)

        // When
        boolean isValid = passwordPolicyService.validatePassword(password);

        // Then
        assertFalse(isValid, "Password with only uppercase and lowercase should be invalid (needs 3 categories total)");
    }

    @Test
    void validatePassword_UpperCaseDigitsOnly_ShouldReturnFalse() {
        // Given
        String password = "PASSWORD123"; // Только верхний + цифры (2 категории)

        // When
        boolean isValid = passwordPolicyService.validatePassword(password);

        // Then
        assertFalse(isValid, "Password with only uppercase and digits should be invalid (needs 3 categories total)");
    }

    @Test
    void validatePassword_ValidComplexPassword_ShouldReturnTrue() {
        // Given
        String password = "MyPass123!"; // Верхний + нижний + цифры + спецсимволы

        // When
        boolean isValid = passwordPolicyService.validatePassword(password);

        // Then
        assertTrue(isValid, "Complex password should be valid");
    }
}
