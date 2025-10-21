package com.bank.config;

import com.bank.service.RateLimitService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("rateLimit");
    }

    @Bean
    public RateLimitService rateLimitService() {
        return new RateLimitService(cacheManager());
    }
}
