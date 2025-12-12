package com.bank.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidTokenExceptionTest {

    @Test
    void constructorWithMessage_ShouldCreateException() {
        String message = "Invalid JWT token";

        InvalidTokenException exception = new InvalidTokenException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageAndCause_ShouldCreateException() {
        String message = "Token validation failed";
        Throwable cause = new RuntimeException("Signature verification failed");

        InvalidTokenException exception = new InvalidTokenException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException() {
        InvalidTokenException exception = new InvalidTokenException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constructorWithNullMessage_ShouldWork() {
        InvalidTokenException exception = new InvalidTokenException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void constructorWithNullMessageAndCause_ShouldWork() {
        InvalidTokenException exception = new InvalidTokenException(null, null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageAndNullCause_ShouldWork() {
        String message = "Token expired";

        InvalidTokenException exception = new InvalidTokenException(message, null);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}
