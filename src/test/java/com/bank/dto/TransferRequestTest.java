package com.bank.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransferRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidation_ValidObject() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.50"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void testValidation_NullFromCardId() {
        TransferRequest request = new TransferRequest();
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.50"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Source card ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullToCardId() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setAmount(new BigDecimal("100.50"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Destination card ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullAmount() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Amount is required", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_ZeroAmount() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.ZERO);

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Amount must be greater than 0", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NegativeAmount() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("-100.00"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Amount must be greater than 0", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_MinimumAmount() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("0.01"));

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void testToString() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.50"));
        String toString = request.toString();
        assertNotNull(toString); // Только проверяем что не null
    }
}
