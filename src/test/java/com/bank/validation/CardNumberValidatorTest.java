package com.bank.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardNumberValidatorTest {

    private CardNumberValidator validator;
    private ConstraintValidatorContext context;
    private ValidCardNumber constraint;

    @BeforeEach
    void setUp() {
        validator = new CardNumberValidator();
        context = mock(ConstraintValidatorContext.class);
        constraint = mock(ValidCardNumber.class);

        when(constraint.spaces()).thenReturn(true);
        when(constraint.dashes()).thenReturn(true);
        when(constraint.luhnCheck()).thenReturn(true);

        validator.initialize(constraint);
    }

    @Test
    void isValid_ValidVisaCard_ShouldReturnTrue() {
        String validVisa = "4111111111111111"; // Valid Luhn number

        boolean result = validator.isValid(validVisa, context);

        assertTrue(result, "Valid Visa card should pass validation");
    }

    @Test
    void isValid_ValidMasterCard_ShouldReturnTrue() {
        String validMasterCard = "5555555555554444"; // Valid Luhn number

        boolean result = validator.isValid(validMasterCard, context);

        assertTrue(result, "Valid MasterCard should pass validation");
    }

    @Test
    void isValid_ValidAmexCard_ShouldReturnTrue() {
        String validAmex = "378282246310005"; // Valid Luhn number

        boolean result = validator.isValid(validAmex, context);

        assertTrue(result, "Valid Amex card should pass validation");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4111-1111-1111-1111",  // With dashes
            "4111 1111 1111 1111",  // With spaces
            "4111-1111 1111-1111",  // Mixed separators
    })
    void isValid_WithSeparators_ShouldReturnTrue(String cardNumber) {
        boolean result = validator.isValid(cardNumber, context);

        assertTrue(result, "Card number with separators should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123456789012",         // Too short (12 digits)
            "12345678901234567890", // Too long (20 digits)
            "4111111111111112",     // Invalid Luhn check
            "1234abcd5678efgh",     // Contains letters
            "1234-5678-90ab-cdef",  // Contains letters with separators
            "!@#$%^&*()_+=-",       // Special characters
            "",                     // Empty string
            "   ",                  // Only whitespace
    })
    void isValid_InvalidCardNumbers_ShouldReturnFalse(String cardNumber) {
        boolean result = validator.isValid(cardNumber, context);

        assertFalse(result, "Invalid card number '" + cardNumber + "' should fail validation");
    }

    @Test
    void isValid_NullValue_ShouldReturnTrue() {
        // Null values are handled by @NotBlank, so validator should return true
        boolean result = validator.isValid(null, context);

        assertTrue(result, "Null should return true (handled by @NotBlank)");
    }

    @Test
    void isValid_WithoutLuhnCheck_ShouldSkipLuhnValidation() {
        // Configure validator without Luhn check
        when(constraint.luhnCheck()).thenReturn(false);
        validator.initialize(constraint);

        String invalidLuhn = "4111111111111112"; // Invalid Luhn

        boolean result = validator.isValid(invalidLuhn, context);

        assertTrue(result, "Without Luhn check, should accept invalid Luhn numbers");
    }

    @Test
    void calculateLuhnCheckDigit_ValidNumber_ShouldReturnCorrectDigit() {
        // Test Visa card without check digit
        String number = "411111111111111";
        int checkDigit = validator.calculateLuhnCheckDigit(number);

        assertEquals(1, checkDigit, "Check digit should be 1 for 411111111111111");
    }

    @Test
    void calculateLuhnCheckDigit_InvalidInput_ShouldReturnNegativeOne() {
        int checkDigit = validator.calculateLuhnCheckDigit("invalid");

        assertEquals(-1, checkDigit, "Invalid input should return -1");
    }

    @Test
    void isTestCardNumber_TestNumbers_ShouldReturnTrue() {
        assertTrue(validator.isTestCardNumber("4111111111111111"), "Visa test card");
        assertTrue(validator.isTestCardNumber("5555555555554444"), "MasterCard test card");
        assertTrue(validator.isTestCardNumber("378282246310005"), "Amex test card");
        assertTrue(validator.isTestCardNumber("6011111111111117"), "Discover test card");
    }

    @Test
    void isTestCardNumber_NonTestNumbers_ShouldReturnFalse() {
        assertFalse(validator.isTestCardNumber("1234567890123456"), "Non-test card");
        assertFalse(validator.isTestCardNumber("invalid"), "Invalid card");
        assertFalse(validator.isTestCardNumber(null), "Null card");
    }

    @Test
    void detectCardType_ShouldReturnCorrectType() {
        assertEquals("VISA", validator.detectCardType("4111111111111111"));
        assertEquals("MASTERCARD", validator.detectCardType("5555555555554444"));
        assertEquals("AMEX", validator.detectCardType("378282246310005"));
        assertEquals("DISCOVER", validator.detectCardType("6011111111111117"));
        assertEquals("UNKNOWN", validator.detectCardType("1234567890123456"));
        assertEquals("UNKNOWN", validator.detectCardType("invalid"));
        assertEquals("UNKNOWN", validator.detectCardType(null));
    }
}
