package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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
        monitoringService.recordSuccessfulLogin("testuser2");
        assertEquals(2, monitoringService.getMetric("login.success"));
    }

    @Test
    void recordFailedLogin_ShouldIncrementCounter() {
        monitoringService.recordFailedLogin("testuser");
        assertEquals(1, monitoringService.getMetric("login.failure"));
        monitoringService.recordFailedLogin("testuser");
        assertEquals(2, monitoringService.getMetric("login.failure"));
    }

    @Test
    void recordTransfer_ShouldIncrementCounter() {
        monitoringService.recordTransfer("user1", "user2", new BigDecimal("100.00"));
        assertEquals(1, monitoringService.getMetric("transfer.completed"));
        monitoringService.recordTransfer("user1", "user3", new BigDecimal("50.00"));
        assertEquals(2, monitoringService.getMetric("transfer.completed"));
    }

    @Test
    void getMetric_NonExistentMetric_ShouldReturnZero() {
        assertEquals(0, monitoringService.getMetric("nonexistent.metric"));
        assertEquals(0, monitoringService.getMetric("unknown"));
    }

    @Test
    void recordCardBlockRequest_ShouldIncrementCounter() {
        monitoringService.recordCardBlockRequest(123L, "testuser");
        assertEquals(1, monitoringService.getMetric("card.block.requests"));
        monitoringService.recordCardBlockRequest(124L, "testuser2");
        assertEquals(2, monitoringService.getMetric("card.block.requests"));
    }

    @Test
    void recordApiRequest_ShouldIncrementCounter() {
        monitoringService.recordApiRequest();
        assertEquals(1, monitoringService.getMetric("api.requests"));
        for (int i = 0; i < 10; i++) {
            monitoringService.recordApiRequest();
        }
        assertEquals(11, monitoringService.getMetric("api.requests"));
    }

    @Test
    void recordTransaction_ShouldIncrementCounter() {
        monitoringService.recordTransaction();
        assertEquals(1, monitoringService.getMetric("transactions.total"));
        monitoringService.recordTransaction();
        monitoringService.recordTransaction();
        assertEquals(3, monitoringService.getMetric("transactions.total"));
    }

    @Test
    void recordError_ShouldIncrementCounter() {
        monitoringService.recordError();
        assertEquals(1, monitoringService.getMetric("errors.total"));
        monitoringService.recordError();
        monitoringService.recordError();
        assertEquals(3, monitoringService.getMetric("errors.total"));
    }

    @Test
    void getAllMetrics_ShouldReturnAllMetrics() {
        ConcurrentHashMap<String, AtomicLong> metrics = monitoringService.getAllMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("login.success"));
        assertTrue(metrics.containsKey("login.failure"));
        assertTrue(metrics.containsKey("transfer.completed"));
        assertTrue(metrics.containsKey("card.block.requests"));
        assertTrue(metrics.containsKey("api.requests"));
        assertTrue(metrics.containsKey("transactions.total"));
        assertTrue(metrics.containsKey("errors.total"));

        assertEquals(0, metrics.get("login.success").get());
        assertEquals(0, metrics.get("login.failure").get());
    }

    @Test
    void recordSystemHealth_ShouldNotThrowException() {
        assertDoesNotThrow(() -> monitoringService.recordSystemHealth());

        // Вызвать несколько раз
        assertDoesNotThrow(() -> monitoringService.recordSystemHealth());
        assertDoesNotThrow(() -> monitoringService.recordSystemHealth());
    }

    @Test
    void getMetric_ForUninitializedMetric_ShouldReturnZero() throws Exception {
        // Получаем доступ к приватному полю через рефлексию
        Field metricsField = MonitoringService.class.getDeclaredField("metrics");
        metricsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, AtomicLong> metrics =
                (ConcurrentHashMap<String, AtomicLong>) metricsField.get(monitoringService);

        // Удаляем одну метрику
        metrics.remove("login.success");

        // Теперь getMetric должен вернуть 0 для несуществующей метрики
        assertEquals(0, monitoringService.getMetric("login.success"));
    }

    @Test
    void constructor_ShouldInitializeAllMetrics() {
        // Проверяем, что все метрики инициализированы
        assertNotNull(monitoringService.getAllMetrics());
        assertEquals(7, monitoringService.getAllMetrics().size());

        // Проверяем каждую метрику отдельно
        assertEquals(0, monitoringService.getMetric("login.success"));
        assertEquals(0, monitoringService.getMetric("login.failure"));
        assertEquals(0, monitoringService.getMetric("transfer.completed"));
        assertEquals(0, monitoringService.getMetric("card.block.requests"));
        assertEquals(0, monitoringService.getMetric("api.requests"));
        assertEquals(0, monitoringService.getMetric("transactions.total"));
        assertEquals(0, monitoringService.getMetric("errors.total"));
    }

    @Test
    void concurrentAccess_ShouldBeThreadSafe() throws InterruptedException {
        int threadCount = 10;
        int iterations = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    monitoringService.recordSuccessfulLogin("user");
                    monitoringService.recordApiRequest();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount * iterations, monitoringService.getMetric("login.success"));
        assertEquals(threadCount * iterations, monitoringService.getMetric("api.requests"));
    }

    @Test
    void recordTransfer_WithNullParameters_ShouldStillIncrement() {
        monitoringService.recordTransfer(null, null, null);
        assertEquals(1, monitoringService.getMetric("transfer.completed"));

        monitoringService.recordTransfer("user1", null, BigDecimal.ZERO);
        assertEquals(2, monitoringService.getMetric("transfer.completed"));
    }

    @Test
    void recordCardBlockRequest_WithNullUsername_ShouldWork() {
        monitoringService.recordCardBlockRequest(123L, null);
        assertEquals(1, monitoringService.getMetric("card.block.requests"));
    }

    @Test
    void recordSuccessfulLogin_WithNullUsername_ShouldWork() {
        monitoringService.recordSuccessfulLogin(null);
        assertEquals(1, monitoringService.getMetric("login.success"));
    }

    @Test
    void recordFailedLogin_WithNullUsername_ShouldWork() {
        monitoringService.recordFailedLogin(null);
        assertEquals(1, monitoringService.getMetric("login.failure"));
    }

    @Test
    void getMetric_AfterMultipleOperations_ShouldReturnCorrectValues() {
        // Выполняем различные операции
        monitoringService.recordSuccessfulLogin("user1");
        monitoringService.recordFailedLogin("user1");
        monitoringService.recordTransfer("user1", "user2", new BigDecimal("100.00"));
        monitoringService.recordCardBlockRequest(1L, "user1");
        monitoringService.recordApiRequest();
        monitoringService.recordTransaction();
        monitoringService.recordError();

        // Проверяем все метрики
        assertEquals(1, monitoringService.getMetric("login.success"));
        assertEquals(1, monitoringService.getMetric("login.failure"));
        assertEquals(1, monitoringService.getMetric("transfer.completed"));
        assertEquals(1, monitoringService.getMetric("card.block.requests"));
        assertEquals(1, monitoringService.getMetric("api.requests"));
        assertEquals(1, monitoringService.getMetric("transactions.total"));
        assertEquals(1, monitoringService.getMetric("errors.total"));
    }
}
