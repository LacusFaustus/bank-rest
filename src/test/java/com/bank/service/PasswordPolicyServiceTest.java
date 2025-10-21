package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyServiceTest {

    private PasswordPolicyService passwordPolicyService;

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyService();
    }

    @Test
    void validatePassword_StrongPassword_ShouldReturnTrue() {
        String strongPassword = "StrongPass123!";
        boolean isValid = passwordPolicyService.validatePassword(strongPassword);
        assertTrue(isValid, "Strong password with all categories should be valid");
    }

    @Test
    void validatePassword_UpperCaseLowerCaseDigits_ShouldReturnTrue() {
        String password = "Password123";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase, lowercase and digits should be valid");
    }

    @Test
    void validatePassword_UpperCaseLowerCaseSpecial_ShouldReturnTrue() {
        String password = "Password!@#";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase, lowercase and special characters should be valid");
    }

    @Test
    void validatePassword_UpperCaseDigitsSpecial_ShouldReturnTrue() {
        String password = "PASSWORD123!";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase, digits and special characters should be valid");
    }

    @Test
    void validatePassword_OnlyUppercaseAndDigits_ShouldReturnTrue() {
        String password = "PASSWORD123";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase and digits should be valid");
    }

    @Test
    void validatePassword_OnlyUppercaseAndSpecial_ShouldReturnTrue() {
        String password = "PASSWORD!@#";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase and special characters should be valid");
    }

    @Test
    void validatePassword_OnlyUppercaseAndLowercase_ShouldReturnTrue() {
        String password = "Password";
        boolean isValid = passwordPolicyService.validatePassword(password);
        assertTrue(isValid, "Password with uppercase and lowercase should be valid");
    }

    @Test
    void validatePassword_TooShort_ShouldReturnFalse() {
        String shortPassword = "Short1!";
        boolean isValid = passwordPolicyService.validatePassword(shortPassword);
        assertFalse(isValid, "Password shorter than 8 characters should be invalid");
    }

    @Test
    void validatePassword_TooLong_ShouldReturnFalse() {
        String longPassword = "A".repeat(129) + "1!";
        boolean isValid = passwordPolicyService.validatePassword(longPassword);
        assertFalse(isValid, "Password longer than 128 characters should be invalid");
    }

    @Test
    void validatePassword_NullPassword_ShouldReturnFalse() {
        boolean isValid = passwordPolicyService.validatePassword(null);
        assertFalse(isValid, "Null password should be invalid");
    }

    @Test
    void validatePassword_EmptyPassword_ShouldReturnFalse() {
        boolean isValid = passwordPolicyService.validatePassword("");
        assertFalse(isValid, "Empty password should be invalid");
    }

    @Test
    void validatePassword_NoUppercase_ShouldReturnFalse() {
        String noUppercase = "lowercase123!";
        boolean isValid = passwordPolicyService.validatePassword(noUppercase);
        assertFalse(isValid, "Password without uppercase should be invalid");
    }

    @Test
    void validatePassword_OnlyUppercase_ShouldReturnFalse() {
        String onlyUppercase = "PASSWORD";
        boolean isValid = passwordPolicyService.validatePassword(onlyUppercase);
        assertFalse(isValid, "Password with only uppercase should be invalid");
    }

    @Test
    void generatePasswordRequirementsMessage_ShouldReturnCorrectMessage() {
        String message = passwordPolicyService.generatePasswordRequirementsMessage();
        assertNotNull(message);
        assertTrue(message.contains("8"));
        assertTrue(message.contains("128"));
        assertTrue(message.contains("uppercase"));
        assertTrue(message.contains("one of the following"));
    }
}
