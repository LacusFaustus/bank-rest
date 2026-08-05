package com.bank.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientBalanceExceptionTest {

    @Test
    void constructorWithMessage_ShouldCreateException() {
        String message = "Insufficient balance: required 1000.00, available 500.00";

        InsufficientBalanceException exception = new InsufficientBalanceException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException() {
        InsufficientBalanceException exception = new InsufficientBalanceException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constructorWithNullMessage_ShouldWork() {
        InsufficientBalanceException exception = new InsufficientBalanceException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void fillInStackTrace_ShouldWork() {
        InsufficientBalanceException exception = new InsufficientBalanceException("Test");

        Throwable filled = exception.fillInStackTrace();

        assertSame(exception, filled);
        assertNotNull(filled.getStackTrace());
    }
}
