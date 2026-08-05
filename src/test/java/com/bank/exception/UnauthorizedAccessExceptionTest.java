package com.bank.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedAccessExceptionTest {

    @Test
    void constructorWithMessage_ShouldCreateException() {
        String message = "Unauthorized access to resource";

        UnauthorizedAccessException exception = new UnauthorizedAccessException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constructorWithNullMessage_ShouldWork() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void stackTrace_ShouldBeAvailable() {
        UnauthorizedAccessException exception = new UnauthorizedAccessException("Test");

        assertDoesNotThrow(() -> {
            StackTraceElement[] stackTrace = exception.getStackTrace();
            // Just verify we can access stack trace without exception
        });
    }
}
