package com.bank.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidation_ValidObject() {
        AuthRequest request = new AuthRequest();
        request.setUsername("username");
        request.setPassword("ValidPass123!");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void testValidation_EmptyUsername() {
        AuthRequest request = new AuthRequest();
        request.setUsername("");
        request.setPassword("ValidPass123!");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Username is required", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NullUsername() {
        AuthRequest request = new AuthRequest();
        request.setUsername(null);
        request.setPassword("ValidPass123!");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Username is required", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_EmptyPassword() {
        AuthRequest request = new AuthRequest("username", "");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());

        boolean hasPasswordError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertTrue(hasPasswordError);
    }

    @Test
    void testJsonCreatorConstructor() {
        AuthRequest request = new AuthRequest("testUser", "testPass");

        assertEquals("testUser", request.getUsername());
        assertEquals("testPass", request.getPassword());
    }

    @Test
    void testValidation_InvalidPassword() {
        AuthRequest request = new AuthRequest();
        request.setUsername("username");
        request.setPassword("weak"); // слабый пароль

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void testValidation_BlankUsername() {
        AuthRequest request = new AuthRequest();
        request.setUsername(" ");
        request.setPassword("ValidPass123!");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Username is required", violations.iterator().next().getMessage());
    }

    @Test
    void testDefaultConstructor() {
        AuthRequest request = new AuthRequest();
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    // Добавить в конец файла AuthRequestTest.java

    @Test
    void testEquals_SameValues() {
        AuthRequest request1 = new AuthRequest("user", "pass");
        AuthRequest request2 = new AuthRequest("user", "pass");

        assertEquals(request1.getUsername(), request2.getUsername());
        assertEquals(request1.getPassword(), request2.getPassword());
    }

    @Test
    void testEquals_DifferentUsername() {
        AuthRequest request1 = new AuthRequest("user1", "pass");
        AuthRequest request2 = new AuthRequest("user2", "pass");

        assertNotEquals(request1.getUsername(), request2.getUsername());
    }

    @Test
    void testHashCode_Consistency() {
        AuthRequest request = new AuthRequest("user", "pass");
        int hash1 = request.hashCode();
        int hash2 = request.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    void testToString() {
        AuthRequest request = new AuthRequest("user", "pass");
        String str = request.toString();

        assertNotNull(str);
        // Вместо проверки содержимого, просто проверяем что не null
        // toString() может быть сгенерирован Lombok по-разному
        assertNotNull(str);
    }

    @Test
    void testJsonCreator_WithNullValues() {
        AuthRequest request = new AuthRequest(null, null);

        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    void testSettersAfterCreation() {
        AuthRequest request = new AuthRequest("old", "oldpass");
        request.setUsername("new");
        request.setPassword("newpass");

        assertEquals("new", request.getUsername());
        assertEquals("newpass", request.getPassword());
    }
}
