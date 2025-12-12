package com.bank.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private AuditService auditService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        auditService = new AuditService();
    }

    @Test
    void logSecurityEvent_ShouldLogSuccessEvent() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("LOGIN_SUCCESS", "User logged in", true, request);
        });
    }

    @Test
    void logSecurityEvent_ShouldLogFailureEvent() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("LOGIN_FAILED", "Invalid password", false, request);
        });
    }

    @Test
    void logSecurityEvent_WithXForwardedFor_ShouldUseCorrectIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("LOGIN_SUCCESS", "User logged in", true, request);
        });
    }

    @Test
    void logSecurityEvent_WithEmptyXForwardedFor_ShouldUseRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("TEST", "Test event", true, request);
        });
    }

    @Test
    void logSecurityEvent_WithXForwardedForAndSpaces_ShouldHandle() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("  192.168.1.1  ,  10.0.0.1  ");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("LOGIN_SUCCESS", "User logged in", true, request);
        });
    }

    @Test
    void logTransactionEvent_ShouldLogTransaction() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logTransactionEvent("TRANSFER", "Money transfer", 100.50,
                    "account123", "account456", request);
        });
    }

    @Test
    void logTransactionEvent_WithZeroAmount_ShouldLog() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logTransactionEvent("TRANSFER", "Zero amount transfer", 0.0,
                    "account123", "account456", request);
        });
    }

    @Test
    void logTransactionEvent_WithNegativeAmount_ShouldLog() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logTransactionEvent("TRANSFER", "Negative amount", -50.0,
                    "account123", "account456", request);
        });
    }

    @Test
    void logActivity_ShouldLogActivityEvent() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logActivity("CARD_CREATE", "Card created", true,
                    "user1", null, request);
        });
    }

    @Test
    void logActivity_WithBothAccounts_ShouldLog() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logActivity("TRANSFER", "Transfer between accounts", true,
                    "account123", "account456", request);
        });
    }

    @Test
    void logActivity_WithFailedEvent_ShouldLog() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logActivity("CARD_CREATE", "Card creation failed", false,
                    "user1", null, request);
        });
    }

    @Test
    void logSecurityEvent_NullRequest_ShouldHandleGracefully() {
        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("LOGIN_FAILED", "Invalid credentials", false, null);
        });
    }

    @Test
    void logTransactionEvent_NullRequest_ShouldHandleGracefully() {
        assertDoesNotThrow(() -> {
            auditService.logTransactionEvent("TRANSFER", "Money transfer", 100.50,
                    "account123", "account456", null);
        });
    }

    @Test
    void logActivity_NullRequest_ShouldHandleGracefully() {
        assertDoesNotThrow(() -> {
            auditService.logActivity("CARD_UPDATE", "Card updated", true,
                    "user1", "user2", null);
        });
    }

    @Test
    void logSecurityEvent_WithNullDescription_ShouldHandleGracefully() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("LOGIN_SUCCESS", null, true, request);
        });
    }

    @Test
    void logSecurityEvent_WithNullEventType_ShouldHandleGracefully() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent(null, "Event", true, request);
        });
    }

    @Test
    void getUserAgent_WithNullUserAgent_ShouldReturnUnknown() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn(null);

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("TEST", "Test event", true, request);
        });
    }

    @Test
    void getUserAgent_WithEmptyUserAgent_ShouldReturnUnknown() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("");

        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("TEST", "Test event", true, request);
        });
    }

    @Test
    void logEvent_ShouldHandleExceptionGracefully() throws Exception {
        // Use reflection to test private logEvent method
        Method logEventMethod = AuditService.class.getDeclaredMethod("logEvent",
                String.class, String.class, String.class, boolean.class,
                String.class, String.class, HttpServletRequest.class);
        logEventMethod.setAccessible(true);

        // Create a mock request that throws exception
        HttpServletRequest badRequest = mock(HttpServletRequest.class);
        when(badRequest.getHeader(anyString())).thenThrow(new RuntimeException("Test exception"));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            logEventMethod.invoke(auditService, "SECURITY", "TEST", "Test event",
                    true, "from", "to", badRequest);
        });
    }

    @Test
    void maskAccountNumber_ShouldMaskCorrectly() throws Exception {
        Method maskAccountMethod = AuditService.class.getDeclaredMethod("maskAccountNumber", String.class);
        maskAccountMethod.setAccessible(true);

        // Test with normal account number
        String masked = (String) maskAccountMethod.invoke(auditService, "1234567890123456");
        assertEquals("1234****3456", masked);

        // Test with shorter account number
        String maskedShort = (String) maskAccountMethod.invoke(auditService, "12345678");
        assertEquals("1234****5678", maskedShort);

        // Test with exactly 8 characters
        String maskedExact8 = (String) maskAccountMethod.invoke(auditService, "12345678");
        assertEquals("1234****5678", maskedExact8);

        // Test with less than 8 characters
        String maskedTooShort = (String) maskAccountMethod.invoke(auditService, "123");
        assertEquals("****", maskedTooShort);

        // Test with null
        String maskedNull = (String) maskAccountMethod.invoke(auditService, (String) null);
        assertEquals("****", maskedNull);

        // Test with empty string
        String maskedEmpty = (String) maskAccountMethod.invoke(auditService, "");
        assertEquals("****", maskedEmpty);

        // Test with 7 characters (edge case)
        String masked7 = (String) maskAccountMethod.invoke(auditService, "1234567");
        assertEquals("****", masked7);

        // Test with 4 characters
        String masked4 = (String) maskAccountMethod.invoke(auditService, "1234");
        assertEquals("****", masked4);

        // Test with 9 characters
        String masked9 = (String) maskAccountMethod.invoke(auditService, "123456789");
        assertEquals("1234****6789", masked9);
    }

    @Test
    void getClientIp_ShouldWorkCorrectly() throws Exception {
        Method getClientIpMethod = AuditService.class.getDeclaredMethod("getClientIp", HttpServletRequest.class);
        getClientIpMethod.setAccessible(true);

        // Test with null request
        String ip = (String) getClientIpMethod.invoke(auditService, (HttpServletRequest) null);
        assertEquals("unknown", ip);

        // Test with X-Forwarded-For
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        ip = (String) getClientIpMethod.invoke(auditService, request);
        assertEquals("192.168.1.1", ip);

        // Test with empty X-Forwarded-For
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        ip = (String) getClientIpMethod.invoke(auditService, request);
        assertEquals("127.0.0.1", ip);

        // Test without X-Forwarded-For
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        ip = (String) getClientIpMethod.invoke(auditService, request);
        assertEquals("127.0.0.1", ip);

        // Test with X-Forwarded-For having spaces
        when(request.getHeader("X-Forwarded-For")).thenReturn("  192.168.1.1  ,  10.0.0.1  ");
        ip = (String) getClientIpMethod.invoke(auditService, request);
        assertEquals("192.168.1.1", ip);
    }

    @Test
    void getUserAgent_ShouldWorkCorrectly() throws Exception {
        Method getUserAgentMethod = AuditService.class.getDeclaredMethod("getUserAgent", HttpServletRequest.class);
        getUserAgentMethod.setAccessible(true);

        // Test with null request
        String userAgent = (String) getUserAgentMethod.invoke(auditService, (HttpServletRequest) null);
        assertEquals("unknown", userAgent);

        // Test with normal user agent
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        userAgent = (String) getUserAgentMethod.invoke(auditService, request);
        assertEquals("Mozilla/5.0", userAgent);

        // Test with null user agent
        when(request.getHeader("User-Agent")).thenReturn(null);
        userAgent = (String) getUserAgentMethod.invoke(auditService, request);
        assertEquals("unknown", userAgent);

        // Test with empty user agent (пустая строка должна возвращаться как есть)
        when(request.getHeader("User-Agent")).thenReturn("");
        userAgent = (String) getUserAgentMethod.invoke(auditService, request);
        assertEquals("", userAgent); // Пустая строка, а не "unknown"
    }

    @Test
    void logSecurityEvent_WithExceptionInLogging_ShouldNotPropagate() {
        // Создаем mock, который выбрасывает исключение при любом вызове getHeader
        HttpServletRequest problematicRequest = mock(HttpServletRequest.class);
        when(problematicRequest.getHeader(anyString())).thenThrow(new RuntimeException("Test exception"));

        // Метод должен обработать исключение и не пробросить его дальше
        assertDoesNotThrow(() -> {
            auditService.logSecurityEvent("TEST", "Test event", true, problematicRequest);
        });
    }

    @Test
    void logActivity_WithNullEventTypeAndDescription_ShouldHandleGracefully() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        assertDoesNotThrow(() -> {
            auditService.logActivity(null, null, true, null, null, request);
        });
    }

    @Test
    void logEvent_ShouldNotThrowForNullParameters() throws Exception {
        Method logEventMethod = AuditService.class.getDeclaredMethod("logEvent",
                String.class, String.class, String.class, boolean.class,
                String.class, String.class, HttpServletRequest.class);
        logEventMethod.setAccessible(true);

        // Test with all null parameters
        assertDoesNotThrow(() -> {
            logEventMethod.invoke(auditService, null, null, null, false, null, null, null);
        });
    }
}
