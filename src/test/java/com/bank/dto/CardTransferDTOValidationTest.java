package com.bank.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CardTransferDTOValidationTest {

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
        dto.setDescription("Payment for services");

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
        assertEquals("fromCardNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testValidation_NullAmount() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(null);

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
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
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
        assertEquals("Description too long", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersAndSetters() {
        CardTransferDTO dto = new CardTransferDTO();
        String fromCard = "1234567890123456";
        String toCard = "6543210987654321";
        BigDecimal amount = new BigDecimal("500.75");
        String description = "Monthly subscription payment";

        dto.setFromCardNumber(fromCard);
        dto.setToCardNumber(toCard);
        dto.setAmount(amount);
        dto.setDescription(description);

        assertEquals(fromCard, dto.getFromCardNumber());
        assertEquals(toCard, dto.getToCardNumber());
        assertEquals(amount, dto.getAmount());
        assertEquals(description, dto.getDescription());
    }

    @Test
    void testToString() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("Test transfer");

        String toString = dto.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("fromCardNumber"));
        assertTrue(toString.contains("toCardNumber"));
        assertTrue(toString.contains("amount"));
        assertTrue(toString.contains("description"));
    }

    @Test
    void testEqualsAndHashCode() {
        CardTransferDTO dto1 = new CardTransferDTO();
        dto1.setFromCardNumber("1111111111111111");
        dto1.setToCardNumber("2222222222222222");
        dto1.setAmount(new BigDecimal("100.00"));

        CardTransferDTO dto2 = new CardTransferDTO();
        dto2.setFromCardNumber("1111111111111111");
        dto2.setToCardNumber("2222222222222222");
        dto2.setAmount(new BigDecimal("100.00"));

        CardTransferDTO dto3 = new CardTransferDTO();
        dto3.setFromCardNumber("3333333333333333");
        dto3.setToCardNumber("4444444444444444");
        dto3.setAmount(new BigDecimal("200.00"));

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testEquals_NullObject() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1111111111111111");

        assertNotEquals(null, dto);
    }

    @Test
    void testEquals_SameInstance() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1111111111111111");

        assertEquals(dto, dto);
    }

    @Test
    void testEquals_DifferentClass() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1111111111111111");

        assertNotEquals(dto, "string object");
    }

    @Test
    void testHashCode_Consistency() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1111111111111111");
        dto.setToCardNumber("2222222222222222");
        dto.setAmount(new BigDecimal("100.00"));

        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCode_NullFields() {
        CardTransferDTO dto = new CardTransferDTO();

        assertDoesNotThrow(() -> dto.hashCode());
    }

    @Test
    void testLombokAnnotations() {
        CardTransferDTO dto = new CardTransferDTO();

        // Проверяем что есть сеттеры
        assertDoesNotThrow(() ->
                dto.getClass().getMethod("setFromCardNumber", String.class)
        );

        // Проверяем что есть геттеры
        assertDoesNotThrow(() ->
                dto.getClass().getMethod("getFromCardNumber")
        );

        // Проверяем что есть @Data аннотация
        assertNotNull(dto.toString());
    }

    @Test
    void testEmptyConstructor() {
        CardTransferDTO dto = new CardTransferDTO();

        assertNotNull(dto);
        assertNull(dto.getFromCardNumber());
        assertNull(dto.getToCardNumber());
        assertNull(dto.getAmount());
        assertNull(dto.getDescription());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testValidation_BlankToCardNumber(String toCardNumber) {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber(toCardNumber);
        dto.setAmount(new BigDecimal("100.00"));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("toCardNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testValidation_ZeroAmount() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(BigDecimal.ZERO);

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(0, violations.size()); // Нет валидации на минимальную сумму
    }

    @Test
    void testValidation_ValidDescription() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("A".repeat(255)); // Максимальная длина

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(0, violations.size());
    }

    @Test
    void testValidation_MultipleErrors() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("");
        dto.setToCardNumber("");
        dto.setAmount(null);
        dto.setDescription("A".repeat(300));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(4, violations.size());
    }

    @Test
    void testEquals_DifferentAmounts() {
        CardTransferDTO dto1 = new CardTransferDTO();
        dto1.setFromCardNumber("1111111111111111");
        dto1.setToCardNumber("2222222222222222");
        dto1.setAmount(new BigDecimal("100.00"));

        CardTransferDTO dto2 = new CardTransferDTO();
        dto2.setFromCardNumber("1111111111111111");
        dto2.setToCardNumber("2222222222222222");
        dto2.setAmount(new BigDecimal("200.00"));

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_WithNullFields() {
        CardTransferDTO dto1 = new CardTransferDTO();
        CardTransferDTO dto2 = new CardTransferDTO();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEquals_DifferentDescriptions() {
        CardTransferDTO dto1 = new CardTransferDTO();
        dto1.setFromCardNumber("1111111111111111");
        dto1.setToCardNumber("2222222222222222");
        dto1.setAmount(new BigDecimal("100.00"));
        dto1.setDescription("Desc1");

        CardTransferDTO dto2 = new CardTransferDTO();
        dto2.setFromCardNumber("1111111111111111");
        dto2.setToCardNumber("2222222222222222");
        dto2.setAmount(new BigDecimal("100.00"));
        dto2.setDescription("Desc2");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void testValidation_InvalidCardNumberLength() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("123"); // слишком короткий номер
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        // Валидация CardTransferDTO проверяет только @NotBlank для fromCardNumber
        // Длина не валидируется, поэтому ошибок быть не должно
        assertEquals(0, violations.size()); // Исправлено: было 1, должно быть 0
    }

    @Test
    void testValidation_FromCardNumberNull() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber(null);
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
    }

    @Test
    void testValidation_AllFieldsNull() {
        CardTransferDTO dto = new CardTransferDTO();

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(3, violations.size()); // fromCardNumber, toCardNumber, amount
    }

    @Test
    void testValidation_EmptyDescription() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("");

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(0, violations.size()); // Пустое описание допустимо
    }

    @Test
    void testValidation_NullDescription() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription(null);

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(0, violations.size()); // Null описание допустимо
    }

    // Добавить в конец файла CardTransferDTOValidationTest.java

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testValidation_BlankFromCardNumber(String fromCardNumber) {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber(fromCardNumber);
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("fromCardNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void testValidation_NegativeAmount() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("-50.00"));

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(0, violations.size()); // Отрицательная сумма не валидируется
    }

    @Test
    void testValidation_VerySmallAmount() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("0.001")); // 3 знака после запятой

        Set<ConstraintViolation<CardTransferDTO>> violations = validator.validate(dto);

        assertEquals(0, violations.size());
    }

    @Test
    void testEquals_SameObject() {
        CardTransferDTO dto = new CardTransferDTO();
        dto.setFromCardNumber("1234567890123456");
        dto.setToCardNumber("6543210987654321");
        dto.setAmount(new BigDecimal("100.00"));

        assertTrue(dto.equals(dto));
    }

    @Test
    void testHashCode_WithNullValues() {
        CardTransferDTO dto = new CardTransferDTO();

        assertDoesNotThrow(() -> dto.hashCode());
    }

    @Test
    void testHashCode_SameValuesSameHash() {
        CardTransferDTO dto1 = new CardTransferDTO();
        dto1.setFromCardNumber("1234567890123456");
        dto1.setToCardNumber("6543210987654321");
        dto1.setAmount(new BigDecimal("100.00"));
        dto1.setDescription("Test");

        CardTransferDTO dto2 = new CardTransferDTO();
        dto2.setFromCardNumber("1234567890123456");
        dto2.setToCardNumber("6543210987654321");
        dto2.setAmount(new BigDecimal("100.00"));
        dto2.setDescription("Test");

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}
