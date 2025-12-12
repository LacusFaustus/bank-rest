package com.bank.controller;

import com.bank.entity.User;
import com.bank.service.TransactionReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private TransactionReportService transactionReportService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void getDailyReport_ShouldReturnReport() {
        // Given
        Map<String, Object> report = new HashMap<>();
        report.put("totalTransactions", 10);
        report.put("totalAmount", 5000.00);
        report.put("date", "2024-01-01");

        when(transactionReportService.getDailyTransactionReport()).thenReturn(report);

        // When
        ResponseEntity<Map<String, Object>> response = reportController.getDailyReport();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(report, response.getBody());
        verify(transactionReportService).getDailyTransactionReport();
    }

    @Test
    void getUserStats_ShouldReturnUserStatistics() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", 1L);
        stats.put("totalTransactions", 5);
        stats.put("totalSpent", 1500.00);
        stats.put("lastTransactionDate", "2024-01-01");

        when(transactionReportService.getUserTransactionStats(1L)).thenReturn(stats);

        // When
        ResponseEntity<Map<String, Object>> response = reportController.getUserStats(user);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(stats, response.getBody());
        verify(transactionReportService).getUserTransactionStats(1L);
    }

    @Test
    void getUserStats_EmptyStats_ShouldReturnEmptyMap() {
        // Given
        User user = User.builder()
                .id(2L)
                .username("newuser")
                .build();

        Map<String, Object> emptyStats = new HashMap<>();
        when(transactionReportService.getUserTransactionStats(2L)).thenReturn(emptyStats);

        // When
        ResponseEntity<Map<String, Object>> response = reportController.getUserStats(user);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void healthCheck_ShouldReturnOk() {
        // When
        ResponseEntity<Map<String, String>> response = reportController.healthCheck();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody().get("status"));
        assertEquals("Bank REST API", response.getBody().get("service"));
    }

    @Test
    void healthCheck_ShouldReturnCorrectStructure() {
        // When
        ResponseEntity<Map<String, String>> response = reportController.healthCheck();

        // Then
        assertNotNull(response);
        Map<String, String> body = response.getBody();

        assertEquals(2, body.size());
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("service"));
        assertEquals("OK", body.get("status"));
        assertEquals("Bank REST API", body.get("service"));
    }

    @Test
    void getDailyReport_Exception_ShouldPropagateException() {
        // Given
        when(transactionReportService.getDailyTransactionReport())
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportController.getDailyReport();
        });
    }

    @Test
    void getUserStats_UserNotFound_ShouldThrowException() {
        // Given
        User user = User.builder()
                .id(999L)
                .username("nonexistent")
                .build();

        when(transactionReportService.getUserTransactionStats(999L))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportController.getUserStats(user);
        });
    }

    @Test
    void getDailyReport_NullReport_ShouldReturnEmptyMap() {
        // Given
        when(transactionReportService.getDailyTransactionReport()).thenReturn(null);

        // When
        ResponseEntity<Map<String, Object>> response = reportController.getDailyReport();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getUserStats_NullStats_ShouldReturnNull() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(transactionReportService.getUserTransactionStats(1L)).thenReturn(null);

        // When
        ResponseEntity<Map<String, Object>> response = reportController.getUserStats(user);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void healthCheck_ShouldAlwaysReturnSameResponse() {
        // When - первый вызов
        ResponseEntity<Map<String, String>> response1 = reportController.healthCheck();

        // Then
        assertNotNull(response1);
        assertEquals("OK", response1.getBody().get("status"));

        // When - второй вызов (должен быть таким же)
        ResponseEntity<Map<String, String>> response2 = reportController.healthCheck();

        // Then
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody(), response2.getBody());
    }
}
