package com.bank.controller;

import com.bank.dto.PaginatedResponse;
import com.bank.entity.AuditLog;
import com.bank.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditController auditController;

    private AuditLog createAuditLog(Long id, String username, String actionType) {
        return AuditLog.builder()
                .id(id)
                .username(username)
                .actionType(actionType)
                .timestamp(LocalDateTime.now())
                .success(true)
                .ipAddress("127.0.0.1")
                .userAgent("Test")
                .build();
    }

    private PaginatedResponse<AuditLog> createPaginatedResponse(List<AuditLog> logs) {
        return PaginatedResponse.of(new PageImpl<>(logs));
    }

    @Test
    void getAuditLogs_ShouldReturnPaginatedLogs() {
        // Given
        AuditLog log1 = createAuditLog(1L, "user1", "LOGIN");
        AuditLog log2 = createAuditLog(2L, "user2", "TRANSFER");
        PaginatedResponse<AuditLog> response = createPaginatedResponse(List.of(log1, log2));

        when(auditLogService.getAuditLogsWithFilters(
                eq(null), eq(null), eq(null),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(0), eq(50)
        )).thenReturn(response);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getAuditLogs(0, 50, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getContent().size());
    }

    @Test
    void getAuditLogs_WithFilters_ShouldApplyFilters() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        AuditLog log = createAuditLog(1L, "testuser", "LOGIN");
        PaginatedResponse<AuditLog> response = createPaginatedResponse(List.of(log));

        when(auditLogService.getAuditLogsWithFilters(
                eq("testuser"), eq("LOGIN"), eq(true),
                eq(startDate), eq(endDate),
                eq(0), eq(50)
        )).thenReturn(response);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getAuditLogs(0, 50, "testuser", "LOGIN", true, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals("testuser", result.getBody().getContent().get(0).getUsername());
    }

    @Test
    void getAuditLogs_WithDefaultDates_ShouldUseDefaultRange() {
        // Given
        AuditLog log = createAuditLog(1L, "user", "ACTION");
        PaginatedResponse<AuditLog> response = createPaginatedResponse(List.of(log));

        when(auditLogService.getAuditLogsWithFilters(
                eq(null), eq(null), eq(null),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(0), eq(50)
        )).thenReturn(response);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getAuditLogs(0, 50, null, null, null, null, null);

        // Then
        assertNotNull(result);
        verify(auditLogService).getAuditLogsWithFilters(
                eq(null), eq(null), eq(null),
                any(LocalDateTime.class),  // Проверяем, что передаются дефолтные даты
                any(LocalDateTime.class),
                eq(0), eq(50)
        );
    }

    @Test
    void getUserAuditLogs_ShouldReturnUserLogs() {
        // Given
        AuditLog log1 = createAuditLog(1L, "testuser", "LOGIN");
        AuditLog log2 = createAuditLog(2L, "testuser", "LOGOUT");
        PaginatedResponse<AuditLog> response = createPaginatedResponse(List.of(log1, log2));

        when(auditLogService.getUserAuditLogs(eq("testuser"), eq(0), eq(50)))
                .thenReturn(response);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getUserAuditLogs("testuser", 0, 50);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getContent().size());
        assertTrue(result.getBody().getContent().stream()
                .allMatch(log -> "testuser".equals(log.getUsername())));
    }

    @Test
    void getUserAuditLogs_EmptyResult_ShouldReturnEmptyPage() {
        // Given
        PaginatedResponse<AuditLog> emptyResponse = PaginatedResponse.of(Page.empty());

        when(auditLogService.getUserAuditLogs(eq("nonexistent"), eq(0), eq(50)))
                .thenReturn(emptyResponse);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getUserAuditLogs("nonexistent", 0, 50);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getContent().isEmpty());
    }

    @Test
    void getAuditLogs_EmptyResult_ShouldReturnEmptyPage() {
        // Given
        PaginatedResponse<AuditLog> emptyResponse = PaginatedResponse.of(Page.empty());

        when(auditLogService.getAuditLogsWithFilters(
                eq(null), eq(null), eq(null),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(0), eq(50)
        )).thenReturn(emptyResponse);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getAuditLogs(0, 50, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getContent().isEmpty());
    }

    @Test
    void getAuditLogs_WithOnlyUsernameFilter_ShouldFilterByUsername() {
        // Given
        AuditLog log = createAuditLog(1L, "specificuser", "LOGIN");
        PaginatedResponse<AuditLog> response = createPaginatedResponse(List.of(log));

        when(auditLogService.getAuditLogsWithFilters(
                eq("specificuser"), eq(null), eq(null),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(0), eq(50)
        )).thenReturn(response);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getAuditLogs(0, 50, "specificuser", null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals("specificuser", result.getBody().getContent().get(0).getUsername());
    }

    @Test
    void getAuditLogs_WithOnlyActionTypeFilter_ShouldFilterByActionType() {
        // Given
        AuditLog log = createAuditLog(1L, "user", "SPECIFIC_ACTION");
        PaginatedResponse<AuditLog> response = createPaginatedResponse(List.of(log));

        when(auditLogService.getAuditLogsWithFilters(
                eq(null), eq("SPECIFIC_ACTION"), eq(null),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(0), eq(50)
        )).thenReturn(response);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getAuditLogs(0, 50, null, "SPECIFIC_ACTION", null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals("SPECIFIC_ACTION", result.getBody().getContent().get(0).getActionType());
    }

    @Test
    void getAuditLogs_WithOnlySuccessFilter_ShouldFilterBySuccess() {
        // Given
        AuditLog log = createAuditLog(1L, "user", "ACTION");
        log.setSuccess(false);
        PaginatedResponse<AuditLog> response = createPaginatedResponse(List.of(log));

        when(auditLogService.getAuditLogsWithFilters(
                eq(null), eq(null), eq(false),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(0), eq(50)
        )).thenReturn(response);

        // When
        ResponseEntity<PaginatedResponse<AuditLog>> result =
                auditController.getAuditLogs(0, 50, null, null, false, null, null);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getContent().size());
        assertFalse(result.getBody().getContent().get(0).isSuccess());
    }

    @Test
    void getAuditLogs_DifferentPageSize_ShouldUseCorrectPageSize() {
        // Given
        PaginatedResponse<AuditLog> response = PaginatedResponse.of(Page.empty());

        when(auditLogService.getAuditLogsWithFilters(
                eq(null), eq(null), eq(null),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(2), eq(100)
        )).thenReturn(response);

        // When
        auditController.getAuditLogs(2, 100, null, null, null, null, null);

        // Then
        verify(auditLogService).getAuditLogsWithFilters(
                eq(null), eq(null), eq(null),
                any(LocalDateTime.class), any(LocalDateTime.class),
                eq(2), eq(100)
        );
    }
}
