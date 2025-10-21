package com.bank.service;

import com.bank.entity.AuditLog;
import com.bank.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository);
        SecurityContextHolder.setContext(securityContext);

        // Настраиваем моки для SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void logActivity_WithRequest_ShouldSaveAuditLog() {
        // Given
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null); // Добавляем этот мок

        // When
        auditService.logActivity("TEST_ACTION", "Test description", true,
                "resource123", "request details", request);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logActivity_WithoutRequest_ShouldSaveAuditLog() {
        // When
        auditService.logActivity("TEST_ACTION", "Test description", true,
                "resource123", "request details", null);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logSecurityEvent_ShouldCallLogActivity() {
        // Given
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        // When
        auditService.logSecurityEvent("LOGIN_SUCCESS", "User logged in", true, request);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logTransferActivity_Success_ShouldCallLogActivity() {
        // Given
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        // When
        auditService.logTransferActivity(1L, 2L, new BigDecimal("100.00"), true, null, request);

        // Then
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logTransferActivity_Failed_ShouldCallLogActivityTwice() {
        // Given
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        // When
        auditService.logTransferActivity(1L, 2L, new BigDecimal("100.00"), false, "Insufficient funds", request);

        // Then
        verify(auditLogRepository, times(2)).save(any(AuditLog.class));
    }
}
