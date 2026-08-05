package com.bank.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AllExceptionsTest {

    static Stream<Arguments> exceptionProvider() {
        return Stream.of(
                Arguments.of(new CardNotFoundException("Card not found"), "Card not found"),
                Arguments.of(new CardAlreadyExistsException("Card exists"), "Card exists"),
                Arguments.of(new InsufficientBalanceException("Insufficient balance"), "Insufficient balance"),
                Arguments.of(new UnauthorizedAccessException("Unauthorized"), "Unauthorized"),
                Arguments.of(new InvalidTokenException("Invalid token"), "Invalid token"),
                Arguments.of(new RateLimitExceededException("Rate limit"), "Rate limit")
        );
    }

    @ParameterizedTest
    @MethodSource("exceptionProvider")
    void allExceptions_ShouldHaveMessage(RuntimeException exception, String expectedMessage) {
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void allExceptions_ShouldBeRuntimeExceptions() {
        assertAll(
                () -> assertTrue(new CardNotFoundException("test") instanceof RuntimeException),
                () -> assertTrue(new CardAlreadyExistsException("test") instanceof RuntimeException),
                () -> assertTrue(new InsufficientBalanceException("test") instanceof RuntimeException),
                () -> assertTrue(new UnauthorizedAccessException("test") instanceof RuntimeException),
                () -> assertTrue(new InvalidTokenException("test") instanceof RuntimeException),
                () -> assertTrue(new RateLimitExceededException("test") instanceof RuntimeException)
        );
    }

    @Test
    void exceptionChaining_ShouldWork() {
        Throwable cause = new RuntimeException("Root cause");
        InvalidTokenException exception = new InvalidTokenException("Token error", cause);

        assertEquals("Token error", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Root cause", exception.getCause().getMessage());
    }
}
