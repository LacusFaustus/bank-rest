package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MonitoringServiceTest {

    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        monitoringService = new MonitoringService();
    }

    @Test
    void recordSuccessfulLogin_ShouldIncrementCounter() {
        monitoringService.recordSuccessfulLogin("testuser");
        assertEquals(1, monitoringService.getMetric("login.success"));
    }

    @Test
    void recordFailedLogin_ShouldIncrementCounter() {
        monitoringService.recordFailedLogin("testuser");
        assertEquals(1, monitoringService.getMetric("login.failure"));
    }

    @Test
    void recordTransfer_ShouldIncrementCounter() {
        monitoringService.recordTransfer("user1", "user2", new BigDecimal("100.00"));
        assertEquals(1, monitoringService.getMetric("transfer.completed"));
    }

    @Test
    void getMetric_NonExistentMetric_ShouldReturnZero() {
        assertEquals(0, monitoringService.getMetric("nonexistent.metric"));
    }
}
