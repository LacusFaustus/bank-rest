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

class CardTransferDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidation_ValidObject() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }

    @Test
    void testValidation_EmptyFromCardNumber() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    void testValidation_NullAmount() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(null);

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    void testValidation_DescriptionTooLong() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("A".repeat(256));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Description too long", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_ValidDescription() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("A".repeat(255));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }
}
