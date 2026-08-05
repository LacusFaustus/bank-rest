package com.bank.service;

import com.bank.dto.PaginatedResponse;
import com.bank.entity.AuditLog;
import com.bank.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLog testAuditLog;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Создаем AuditLog
        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setUsername("testuser");
        testAuditLog.setActionType("LOGIN");
        testAuditLog.setSuccess(true);
        testAuditLog.setIpAddress("192.168.1.1");
        testAuditLog.setUserAgent("Test Browser");
        testAuditLog.setDescription("User logged in successfully");
        testAuditLog.setTimestamp(now);
    }

    @Test
    void getAuditLogsWithFilters_WithAllParameters_ShouldWork() {
        // Given
        String username = "testuser";
        String actionType = "LOGIN";
        Boolean success = true;
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now;
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findWithFilters(
                eq(username), eq(actionType), eq(success),
                eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                username, actionType, success, startDate, endDate, page, size);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertEquals("testuser", response.getContent().get(0).getUsername());
        assertEquals("LOGIN", response.getContent().get(0).getActionType());

        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getCurrentPage());
        assertEquals(10, response.getPagination().getPageSize());
        assertEquals(1, response.getPagination().getTotalElements());
        assertEquals(1, response.getPagination().getTotalPages());
        assertTrue(response.getPagination().isFirst());
        assertTrue(response.getPagination().isLast());

        verify(auditLogRepository, times(1)).findWithFilters(
                eq(username), eq(actionType), eq(success),
                eq(startDate), eq(endDate), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithNullParameters_ShouldWork() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                null, null, null, null, null, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertNotNull(response.getPagination());
        assertEquals(1, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithEmptyResult_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(auditLogRepository.findWithFilters(any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                "nonexistent", "UNKNOWN", false, now.minusDays(30), now, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getTotalElements());
        assertEquals(0, response.getPagination().getTotalPages());
        assertTrue(response.getPagination().isFirst());
        assertTrue(response.getPagination().isLast());
    }

    @Test
    void getAuditLogsWithFilters_WithOnlyUsername_ShouldWork() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findWithFilters(
                eq("testuser"), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                "testuser", null, null, null, null, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertNotNull(response.getPagination());
        assertEquals(1, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findWithFilters(
                eq("testuser"), isNull(), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    void getUserAuditLogs_ShouldReturnUserLogs() {
        // Given
        String username = "testuser";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findByUsername(eq(username), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getUserAuditLogs(username, page, size);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertEquals(username, response.getContent().get(0).getUsername());

        assertNotNull(response.getPagination());
        assertEquals(1, response.getPagination().getTotalElements());
        assertEquals(0, response.getPagination().getCurrentPage());
        assertEquals(10, response.getPagination().getPageSize());

        verify(auditLogRepository, times(1)).findByUsername(eq(username), eq(pageable));
    }

    @Test
    void getUserAuditLogs_WithNullUsername_ShouldWork() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(auditLogRepository.findByUsername(isNull(), eq(pageable)))
                .thenReturn(emptyPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getUserAuditLogs(null, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findByUsername(isNull(), eq(pageable));
    }

    @Test
    void getUserAuditLogs_WithNoLogs_ShouldReturnEmpty() {
        // Given
        String username = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(auditLogRepository.findByUsername(eq(username), eq(pageable)))
                .thenReturn(emptyPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getUserAuditLogs(username, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getTotalElements());
    }

    @Test
    void getUserAuditLogs_WithMultiplePages_ShouldWork() {
        // Given
        String username = "testuser";
        int page = 1;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        // Создаем 3 записи для второй страницы
        AuditLog log1 = new AuditLog();
        log1.setId(6L);
        log1.setUsername(username);

        AuditLog log2 = new AuditLog();
        log2.setId(7L);
        log2.setUsername(username);

        AuditLog log3 = new AuditLog();
        log3.setId(8L);
        log3.setUsername(username);

        List<AuditLog> auditLogs = Arrays.asList(log1, log2, log3);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, 23); // всего 23 записи

        when(auditLogRepository.findByUsername(eq(username), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getUserAuditLogs(username, page, size);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(3, response.getContent().size()); // 3 элемента на странице

        assertNotNull(response.getPagination());
        assertEquals(23, response.getPagination().getTotalElements());
        assertEquals(1, response.getPagination().getCurrentPage());
        assertEquals(5, response.getPagination().getPageSize());
        assertEquals(5, response.getPagination().getTotalPages()); // 23 / 5 = 5 страниц
        assertFalse(response.getPagination().isFirst());
        assertFalse(response.getPagination().isLast());

        verify(auditLogRepository, times(1)).findByUsername(eq(username), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithLargePageSize_ShouldWork() {
        // Given
        int page = 0;
        int size = 1000; // больший размер страницы
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(auditLogRepository.findWithFilters(any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                null, null, null, null, null, page, size);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getTotalElements());
        assertEquals(1000, response.getPagination().getPageSize());
        verify(auditLogRepository, times(1)).findWithFilters(
                any(), any(), any(), any(), any(), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithEdgeCaseDates_ShouldWork() {
        // Given
        LocalDateTime startDate = LocalDateTime.MIN;
        LocalDateTime endDate = LocalDateTime.MAX;
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findWithFilters(
                any(), any(), any(), eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                null, null, null, startDate, endDate, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertNotNull(response.getPagination());
        assertEquals(1, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findWithFilters(
                any(), any(), any(), eq(startDate), eq(endDate), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithOnlyActionType_ShouldWork() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findWithFilters(
                isNull(), eq("LOGIN"), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                null, "LOGIN", null, null, null, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertNotNull(response.getPagination());
        assertEquals(1, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findWithFilters(
                isNull(), eq("LOGIN"), isNull(), isNull(), isNull(), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithOnlySuccessFlag_ShouldWork() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findWithFilters(
                isNull(), isNull(), eq(true), isNull(), isNull(), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                null, null, true, null, null, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertNotNull(response.getPagination());
        assertEquals(1, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findWithFilters(
                isNull(), isNull(), eq(true), isNull(), isNull(), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithOnlyDateRange_ShouldWork() {
        // Given
        LocalDateTime startDate = now.minusDays(7);
        LocalDateTime endDate = now;
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        Page<AuditLog> auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findWithFilters(
                isNull(), isNull(), isNull(), eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(auditLogPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                null, null, null, startDate, endDate, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(1, response.getContent().size());
        assertNotNull(response.getPagination());
        assertEquals(1, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findWithFilters(
                isNull(), isNull(), isNull(), eq(startDate), eq(endDate), eq(pageable));
    }

    @Test
    void getAuditLogsWithFilters_WithReversedDateRange_ShouldWork() {
        // Given
        LocalDateTime startDate = now;
        LocalDateTime endDate = now.minusDays(1); // endDate раньше startDate
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(auditLogRepository.findWithFilters(
                any(), any(), any(), eq(startDate), eq(endDate), any()))
                .thenReturn(emptyPage);

        // When
        PaginatedResponse<AuditLog> response = auditLogService.getAuditLogsWithFilters(
                null, null, null, startDate, endDate, 0, 10);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertTrue(response.getContent().isEmpty());
        assertNotNull(response.getPagination());
        assertEquals(0, response.getPagination().getTotalElements());
        verify(auditLogRepository, times(1)).findWithFilters(
                any(), any(), any(), eq(startDate), eq(endDate), any());
    }
}
