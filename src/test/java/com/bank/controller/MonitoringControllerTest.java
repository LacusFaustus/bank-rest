package com.bank.controller;

import com.bank.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private MonitoringController monitoringController;

    @Test
    void getMetrics_ShouldReturnMetrics() {
        // Given
        ConcurrentHashMap<String, AtomicLong> metrics = new ConcurrentHashMap<>();
        metrics.put("login.success", new AtomicLong(10));
        metrics.put("login.failure", new AtomicLong(2));
        metrics.put("transactions.total", new AtomicLong(50));
        metrics.put("api.requests", new AtomicLong(1000));

        when(monitoringService.getAllMetrics()).thenReturn(metrics);

        // When
        ResponseEntity<Map<String, Long>> response = monitoringController.getMetrics();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().get("login.success"));
        assertEquals(2L, response.getBody().get("login.failure"));
        assertEquals(50L, response.getBody().get("transactions.total"));
        assertEquals(1000L, response.getBody().get("api.requests"));
        assertEquals(4, response.getBody().size());
    }

    @Test
    void getMetrics_EmptyMetrics_ShouldReturnEmptyMap() {
        // Given
        ConcurrentHashMap<String, AtomicLong> emptyMetrics = new ConcurrentHashMap<>();
        when(monitoringService.getAllMetrics()).thenReturn(emptyMetrics);

        // When
        ResponseEntity<Map<String, Long>> response = monitoringController.getMetrics();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getHealth_ShouldReturnHealthInfo() {
        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getHealth();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals("UP", body.get("status"));
        assertNotNull(body.get("timestamp"));
        assertTrue((Long) body.get("timestamp") > 0);

        @SuppressWarnings("unchecked")
        Map<String, Object> memoryUsage = (Map<String, Object>) body.get("memoryUsage");
        assertNotNull(memoryUsage);
        assertNotNull(memoryUsage.get("usedMemoryMB"));
        assertNotNull(memoryUsage.get("maxMemoryMB"));
        assertNotNull(memoryUsage.get("usagePercent"));

        String usagePercent = (String) memoryUsage.get("usagePercent");
        assertTrue(usagePercent.endsWith("%"));
    }

    @Test
    void getHealth_ShouldCalculateMemoryUsageCorrectly() {
        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getHealth();

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> memoryUsage = (Map<String, Object>) response.getBody().get("memoryUsage");

        long usedMemoryMB = (Long) memoryUsage.get("usedMemoryMB");
        long maxMemoryMB = (Long) memoryUsage.get("maxMemoryMB");
        String usagePercent = (String) memoryUsage.get("usagePercent");

        // Проверяем разумные границы
        assertTrue(usedMemoryMB >= 0);
        assertTrue(maxMemoryMB > 0);
        assertTrue(usedMemoryMB <= maxMemoryMB);

        // Проверяем формат процентов
        assertNotNull(usagePercent);
        assertTrue(usagePercent.contains("%"));
    }
}
