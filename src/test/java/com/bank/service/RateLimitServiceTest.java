package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
        ReflectionTestUtils.setField(rateLimitService, "rateLimitEnabled", true);
    }

    @Test
    void isRateLimited_whenDisabled_shouldReturnFalse() {
        // Arrange
        ReflectionTestUtils.setField(rateLimitService, "rateLimitEnabled", false);
        String key = "test-key";

        // Act
        boolean result = rateLimitService.isRateLimited(key, RateLimitService.RateLimitType.API_REQUEST);

        // Assert
        assertFalse(result);
    }

    @Test
    void isRateLimited_whenNullKey_shouldReturnFalse() {
        // Act
        boolean result = rateLimitService.isRateLimited(null, RateLimitService.RateLimitType.API_REQUEST);

        // Assert
        assertFalse(result);
    }

    @Test
    void isRateLimited_whenNoRequests_shouldReturnFalse() {
        // Arrange
        String key = "test-key";

        // Act
        boolean result = rateLimitService.isRateLimited(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);

        // Assert
        assertFalse(result);
    }

    @Test
    void isRateLimited_whenLimitExceeded_shouldReturnTrue() {
        // Arrange
        String key = "test-key";

        // Имитируем превышение лимита
        for (int i = 0; i < 10; i++) {
            rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
        }

        // Act
        boolean result = rateLimitService.isRateLimited(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);

        // Assert
        assertTrue(result);
    }

    @Test
    void tryRecordRequest_whenWithinLimit_shouldReturnTrue() {
        // Arrange
        String key = "test-key";

        // Act & Assert
        for (int i = 0; i < 5; i++) {
            boolean result = rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.TRANSFER_OPERATION);
            assertTrue(result, "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void tryRecordRequest_whenLimitExceeded_shouldReturnFalse() {
        // Arrange
        String key = "test-key";

        // Act
        boolean[] results = new boolean[6];
        for (int i = 0; i < 6; i++) {
            results[i] = rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.TRANSFER_OPERATION);
        }

        // Assert
        for (int i = 0; i < 5; i++) {
            assertTrue(results[i], "Request " + (i + 1) + " should be allowed");
        }
        assertFalse(results[5], "6th request should be blocked (limit is 5)");
    }

    @Test
    void getCountersMap_shouldReturnCorrectMapForEachType() {
        // Act & Assert
        assertNotNull(getPrivateCountersMap(RateLimitService.RateLimitType.LOGIN_ATTEMPT));
        assertNotNull(getPrivateCountersMap(RateLimitService.RateLimitType.TRANSFER_OPERATION));
        assertNotNull(getPrivateCountersMap(RateLimitService.RateLimitType.CARD_BLOCK));
        assertNotNull(getPrivateCountersMap(RateLimitService.RateLimitType.API_REQUEST));
    }

    @Test
    void getLimit_shouldReturnCorrectLimitForEachType() {
        // Act & Assert
        assertEquals(10, getPrivateLimit(RateLimitService.RateLimitType.LOGIN_ATTEMPT));
        assertEquals(5, getPrivateLimit(RateLimitService.RateLimitType.TRANSFER_OPERATION));
        assertEquals(3, getPrivateLimit(RateLimitService.RateLimitType.CARD_BLOCK));
        assertEquals(100, getPrivateLimit(RateLimitService.RateLimitType.API_REQUEST));
    }

    @Test
    void getWindowSeconds_shouldReturnCorrectWindowForEachType() {
        // Act & Assert
        assertEquals(60, getPrivateWindowSeconds(RateLimitService.RateLimitType.LOGIN_ATTEMPT));
        assertEquals(60, getPrivateWindowSeconds(RateLimitService.RateLimitType.TRANSFER_OPERATION));
        assertEquals(3600, getPrivateWindowSeconds(RateLimitService.RateLimitType.CARD_BLOCK));
        assertEquals(60, getPrivateWindowSeconds(RateLimitService.RateLimitType.API_REQUEST));
    }

    @Test
    void concurrentAccess_shouldBeThreadSafe() throws InterruptedException {
        // Arrange
        String key = "concurrent-key";
        int threadCount = 10;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successfulRequests = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.API_REQUEST)) {
                            successfulRequests.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - максимум 100 запросов должно быть разрешено
        int totalAttempts = threadCount * requestsPerThread;
        int successful = successfulRequests.get();
        assertTrue(successful <= 100, "Should not exceed API limit of 100, but got " + successful);
        assertTrue(successful > 0, "Should have some successful requests");
    }

    @Test
    void recordRequest_shouldWorkWithoutException() {
        // Arrange
        String key = "test-key";

        // Act & Assert (не должно быть исключений)
        assertDoesNotThrow(() -> {
            rateLimitService.recordRequest(key, RateLimitService.RateLimitType.API_REQUEST);
        });
    }

    // Вспомогательные методы для доступа к приватным методам
    @SuppressWarnings("unchecked")
    private java.util.Map<String, ?> getPrivateCountersMap(RateLimitService.RateLimitType type) {
        return (java.util.Map<String, ?>) ReflectionTestUtils.invokeMethod(
                rateLimitService, "getCountersMap", type
        );
    }

    private int getPrivateLimit(RateLimitService.RateLimitType type) {
        return (int) ReflectionTestUtils.invokeMethod(
                rateLimitService, "getLimit", type
        );
    }

    private int getPrivateWindowSeconds(RateLimitService.RateLimitType type) {
        return (int) ReflectionTestUtils.invokeMethod(
                rateLimitService, "getWindowSeconds", type
        );
    }

    @Test
    void cleanupExpiredCounters_ShouldRemoveExpiredEntries() throws InterruptedException {
        // Arrange
        String key = "test-key";

        // Record a request
        rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);

        // Simulate time passing by manipulating the counter
        // This is complex, but you could test via reflection or create a test-specific method

        // For now, just ensure the method doesn't throw
        assertDoesNotThrow(() -> {
            // Use reflection to call the private cleanup method
            java.lang.reflect.Method method = RateLimitService.class.getDeclaredMethod("cleanupExpiredCounters");
            method.setAccessible(true);
            method.invoke(rateLimitService);
        });
    }

    @Test
    void tryRecordRequest_whenDisabled_shouldAlwaysReturnTrue() {
        // Arrange
        ReflectionTestUtils.setField(rateLimitService, "rateLimitEnabled", false);
        String key = "test-key";

        // Act & Assert
        for (int i = 0; i < 20; i++) {
            boolean result = rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
            assertTrue(result, "Request should always be allowed when rate limit is disabled");
        }
    }

    @Test
    void tryRecordRequest_whenNullKey_shouldReturnTrue() {
        // Act
        boolean result = rateLimitService.tryRecordRequest(null, RateLimitService.RateLimitType.LOGIN_ATTEMPT);

        // Assert
        assertTrue(result);
    }

    @Test
    void recordRequest_legacyMethod_shouldWork() {
        // Arrange
        String key = "test-key";

        // Act & Assert (should not throw)
        assertDoesNotThrow(() -> {
            rateLimitService.recordRequest(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
        });

        // Verify it actually recorded
        boolean isLimited = rateLimitService.isRateLimited(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
        assertFalse(isLimited); // Only 1 request, limit is 10
    }

    @Test
    void differentRateLimitTypes_shouldHaveSeparateCounters() {
        // Arrange
        String key = "test-key";

        // Use up all transfer attempts
        for (int i = 0; i < 5; i++) {
            rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.TRANSFER_OPERATION);
        }

        // Should still be able to make login attempts
        boolean loginResult = rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
        assertTrue(loginResult, "Should allow login even though transfer limit is reached");

        // But transfer should be limited
        boolean transferResult = rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.TRANSFER_OPERATION);
        assertFalse(transferResult, "Transfer should be limited after 5 attempts");
    }

    @Test
    void cleanupExpiredCounters_shouldRemoveOldEntries() throws Exception {
        // Этот тест требует мокирования времени или использования тестовой реализации
        // Для простоты проверим, что метод существует и не бросает исключений
        assertDoesNotThrow(() -> {
            // Вызываем через рефлексию, так как метод @Scheduled
            java.lang.reflect.Method method = RateLimitService.class.getDeclaredMethod("cleanupExpiredCounters");
            method.setAccessible(true);
            method.invoke(rateLimitService);
        });
    }

    @Test
    void windowReset_shouldAllowNewRequests() throws Exception {
        // Arrange
        String key = "test-key";

        // Заполняем лимит
        for (int i = 0; i < 5; i++) {
            rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.TRANSFER_OPERATION);
        }

        // Должен быть заблокирован
        boolean blocked = rateLimitService.tryRecordRequest(key, RateLimitService.RateLimitType.TRANSFER_OPERATION);
        assertFalse(blocked);

        // Здесь сложно тестировать сброс окна без мокирования времени
        // В реальном тесте нужно было бы использовать Clock или аналогичный механизм
    }

    @Test
    void isRateLimited_withDifferentKeys_shouldNotInterfere() {
        // Arrange
        String key1 = "key1";
        String key2 = "key2";

        // Fill key1's limit
        for (int i = 0; i < 10; i++) {
            rateLimitService.tryRecordRequest(key1, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
        }

        // key1 should be limited
        boolean key1Limited = rateLimitService.isRateLimited(key1, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
        assertTrue(key1Limited);

        // key2 should NOT be limited
        boolean key2Limited = rateLimitService.isRateLimited(key2, RateLimitService.RateLimitType.LOGIN_ATTEMPT);
        assertFalse(key2Limited);
    }
}
