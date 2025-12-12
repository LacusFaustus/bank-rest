package com.bank.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardNotFoundExceptionTest {

    @Test
    void constructorWithMessage_ShouldCreateException() {
        String message = "Card with ID 123 not found";

        CardNotFoundException exception = new CardNotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException() {
        CardNotFoundException exception = new CardNotFoundException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constructorWithNullMessage_ShouldWork() {
        CardNotFoundException exception = new CardNotFoundException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void getLocalizedMessage_ShouldReturnMessage() {
        String message = "Card not found";
        CardNotFoundException exception = new CardNotFoundException(message);

        assertEquals(message, exception.getLocalizedMessage());
    }
}
