package com.bank.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimitExceededExceptionTest {

    @Test
    void constructorWithMessage_ShouldCreateException() {
        String message = "Rate limit exceeded: 100 requests per minute";

        RateLimitExceededException exception = new RateLimitExceededException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageAndCause_ShouldCreateException() {
        String message = "Rate limit exceeded";
        Throwable cause = new RuntimeException("Underlying cause");

        RateLimitExceededException exception = new RateLimitExceededException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException() {
        RateLimitExceededException exception = new RateLimitExceededException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constructorWithNullMessage_ShouldWork() {
        RateLimitExceededException exception = new RateLimitExceededException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void constructorWithNullMessageAndCause_ShouldWork() {
        RateLimitExceededException exception = new RateLimitExceededException(null, null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructorWithMessageAndNullCause_ShouldWork() {
        String message = "Test message";

        RateLimitExceededException exception = new RateLimitExceededException(message, null);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}
