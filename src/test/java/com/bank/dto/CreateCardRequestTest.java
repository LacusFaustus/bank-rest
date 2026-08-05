package com.bank.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class CreateCardRequestTest {

    private Validator validator;
    private CreateCardRequest request;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        request = new CreateCardRequest();
    }

    @Test
    void testValidation_ValidObject() {
        request.setCardNumber("1234567890123456");
        request.setCardHolder("John Doe");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setUserId(1L);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void testValidation_InvalidCardNumber() {
        request.setCardNumber("123"); // слишком короткий
        request.setCardHolder("John Doe");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setUserId(1L);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Card number must be 16 digits", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_CardNumberWithLetters() {
        request.setCardNumber("123456789012345a"); // содержит букву
        request.setCardHolder("John Doe");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setUserId(1L);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Card number must be 16 digits", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_CardHolderTooShort() {
        request.setCardNumber("1234567890123456");
        request.setCardHolder("A"); // слишком короткое
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setUserId(1L);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Card holder name must be between 2 and 100 characters", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_PastExpiryDate() {
        request.setCardNumber("1234567890123456");
        request.setCardHolder("John Doe");
        request.setExpiryDate(LocalDate.now().minusDays(1)); // дата в прошлом
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setUserId(1L);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Expiry date must be in the future", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NegativeBalance() {
        request.setCardNumber("1234567890123456");
        request.setCardHolder("John Doe");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(new BigDecimal("-100.00")); // отрицательный баланс
        request.setUserId(1L);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Balance must be positive", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullUserId() {
        request.setCardNumber("1234567890123456");
        request.setCardHolder("John Doe");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setUserId(null); // null userId

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("User ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_ZeroBalance() {
        request.setCardNumber("1234567890123456");
        request.setCardHolder("John Doe");
        request.setExpiryDate(LocalDate.now().plusYears(1));
        request.setInitialBalance(BigDecimal.ZERO); // нулевой баланс допустим
        request.setUserId(1L);

        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);
        assertEquals(0, violations.size());
    }
}
