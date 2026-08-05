package com.bank.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StrongPasswordValidatorTest {

    private StrongPasswordValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new StrongPasswordValidator();
        context = mock(ConstraintValidatorContext.class);
        validator.initialize(mock(StrongPassword.class));
    }

    @Test
    void isValid_StrongPassword_ShouldReturnTrue() {
        String strongPassword = "StrongPass123!";

        boolean result = validator.isValid(strongPassword, context);

        assertTrue(result, "Strong password should be valid");
    }

    @Test
    void isValid_PasswordTooShort_ShouldReturnFalse() {
        String shortPassword = "Short1!";

        boolean result = validator.isValid(shortPassword, context);

        assertFalse(result, "Password shorter than 8 characters should be invalid");
    }

    @Test
    void isValid_PasswordNoDigits_ShouldReturnFalse() {
        String noDigits = "PasswordNoDigits!";

        boolean result = validator.isValid(noDigits, context);

        assertFalse(result, "Password without digits should be invalid");
    }

    @Test
    void isValid_PasswordNoLowercase_ShouldReturnFalse() {
        String noLowercase = "PASSWORD123!";

        boolean result = validator.isValid(noLowercase, context);

        assertFalse(result, "Password without lowercase should be invalid");
    }

    @Test
    void isValid_PasswordNoUppercase_ShouldReturnFalse() {
        String noUppercase = "password123!";

        boolean result = validator.isValid(noUppercase, context);

        assertFalse(result, "Password without uppercase should be invalid");
    }

    @Test
    void isValid_PasswordNoSpecialChar_ShouldReturnFalse() {
        String noSpecialChar = "Password123";

        boolean result = validator.isValid(noSpecialChar, context);

        assertFalse(result, "Password without special characters should be invalid");
    }

    @Test
    void isValid_NullPassword_ShouldReturnFalse() {
        boolean result = validator.isValid(null, context);

        assertFalse(result, "Null password should be invalid");
    }

    @Test
    void isValid_EmptyPassword_ShouldReturnFalse() {
        boolean result = validator.isValid("", context);

        assertFalse(result, "Empty password should be invalid");
    }

    @Test
    void isValid_WhitespaceOnly_ShouldReturnFalse() {
        boolean result = validator.isValid("   ", context);

        assertFalse(result, "Whitespace-only password should be invalid");
    }

    @Test
    void isValid_PasswordWithAllRequirements_ComplexCases() {
        // Test various combinations
        assertTrue(validator.isValid("A1!bcdefg", context), "Minimum length with all requirements");
        assertTrue(validator.isValid("Abc123!@#", context), "Multiple special characters");
        assertTrue(validator.isValid("P@ssw0rd", context), "Common password pattern");
        assertTrue(validator.isValid("MyP@ssw0rd123!", context), "Longer password with all requirements");
    }

    @Test
    void isValid_EdgeCaseSpecialCharacters() {
        // Test all special characters
        String specialChars = "Pass123!";
        assertTrue(validator.isValid(specialChars, context));

        String specialChars2 = "Pass123@";
        assertTrue(validator.isValid(specialChars2, context));

        String specialChars3 = "Pass123#";
        assertTrue(validator.isValid(specialChars3, context));

        String specialChars4 = "Pass123$";
        assertTrue(validator.isValid(specialChars4, context));

        String specialChars5 = "Pass123%";
        assertTrue(validator.isValid(specialChars5, context));

        String specialChars6 = "Pass123^";
        assertTrue(validator.isValid(specialChars6, context));

        String specialChars7 = "Pass123&";
        assertTrue(validator.isValid(specialChars7, context));

        String specialChars8 = "Pass123*";
        assertTrue(validator.isValid(specialChars8, context));
    }
}
