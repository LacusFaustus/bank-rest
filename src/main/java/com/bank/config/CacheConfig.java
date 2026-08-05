package com.bank.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Profile("local")
    public CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager(
                "rateLimit",  // Добавлено
                "userCache",
                "cardCache"
        );
    }

    @Bean
    @Profile("prod")
    public CacheManager prodCacheManager() {
        // Для продакшена можно использовать Redis CacheManager
        // return RedisCacheManager.builder(redisConnectionFactory)
        //         .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
        //                 .entryTtl(Duration.ofMinutes(10)))
        //         .build();

        // Временно используем in-memory кэш
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                "rateLimit",  // Добавлено
                "userCache",
                "cardCache",
                "transactionCache"
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
