package com.bank.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CacheConfigTest {

    private final CacheConfig cacheConfig = new CacheConfig();

    @Test
    void localCacheManager_shouldReturnConcurrentMapCacheManager() {
        // Act
        CacheManager cacheManager = cacheConfig.localCacheManager();

        // Assert
        assertNotNull(cacheManager);
        assertInstanceOf(ConcurrentMapCacheManager.class, cacheManager);

        ConcurrentMapCacheManager concurrentCacheManager = (ConcurrentMapCacheManager) cacheManager;

        // Проверяем что кэши доступны
        assertNotNull(concurrentCacheManager.getCache("rateLimit"));
        assertNotNull(concurrentCacheManager.getCache("userCache"));
        assertNotNull(concurrentCacheManager.getCache("cardCache"));

        // Проверяем что нет лишних кэшей
        assertNull(concurrentCacheManager.getCache("transactionCache")); // Не должно быть в local
    }

    @Test
    void prodCacheManager_shouldReturnConcurrentMapCacheManager() {
        // Act
        CacheManager cacheManager = cacheConfig.prodCacheManager();

        // Assert
        assertNotNull(cacheManager);
        assertInstanceOf(ConcurrentMapCacheManager.class, cacheManager);

        ConcurrentMapCacheManager concurrentCacheManager = (ConcurrentMapCacheManager) cacheManager;

        // Проверяем что все кэши доступны
        assertNotNull(concurrentCacheManager.getCache("rateLimit"));
        assertNotNull(concurrentCacheManager.getCache("userCache"));
        assertNotNull(concurrentCacheManager.getCache("cardCache"));
        assertNotNull(concurrentCacheManager.getCache("transactionCache")); // Должен быть в prod
    }

    @Test
    void localCacheManager_shouldHaveCorrectCacheNames() {
        // Act
        CacheManager cacheManager = cacheConfig.localCacheManager();

        // Assert
        ConcurrentMapCacheManager concurrentCacheManager = (ConcurrentMapCacheManager) cacheManager;

        // Получаем имена кэшей через reflection или проверяем доступность
        var cacheNames = concurrentCacheManager.getCacheNames();
        assertTrue(cacheNames.contains("rateLimit"));
        assertTrue(cacheNames.contains("userCache"));
        assertTrue(cacheNames.contains("cardCache"));
        assertFalse(cacheNames.contains("transactionCache")); // Не должно быть в local
    }

    @Test
    void prodCacheManager_shouldHaveTransactionCache() {
        // Act
        CacheManager cacheManager = cacheConfig.prodCacheManager();

        // Assert
        ConcurrentMapCacheManager concurrentCacheManager = (ConcurrentMapCacheManager) cacheManager;
        var cacheNames = concurrentCacheManager.getCacheNames();
        assertTrue(cacheNames.contains("transactionCache")); // Должен быть в prod
    }
}
