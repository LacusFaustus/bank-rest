package com.bank.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardAlreadyExistsExceptionTest {

    @Test
    void constructorWithMessage_ShouldCreateException() {
        String message = "Card with number 1234567890123456 already exists";

        CardAlreadyExistsException exception = new CardAlreadyExistsException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException() {
        CardAlreadyExistsException exception = new CardAlreadyExistsException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constructorWithNullMessage_ShouldWork() {
        CardAlreadyExistsException exception = new CardAlreadyExistsException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void toString_ShouldIncludeMessage() {
        String message = "Duplicate card";
        CardAlreadyExistsException exception = new CardAlreadyExistsException(message);

        String toString = exception.toString();

        assertNotNull(toString);
        assertTrue(toString.contains(message));
    }
}
