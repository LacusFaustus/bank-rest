package com.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RateLimitService {

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    // Константы лимитов
    private static final int LOGIN_ATTEMPT_LIMIT = 10;
    private static final int LOGIN_ATTEMPT_WINDOW_SECONDS = 60;
    private static final int TRANSFER_LIMIT = 5;
    private static final int TRANSFER_WINDOW_SECONDS = 60;
    private static final int CARD_BLOCK_LIMIT = 3;
    private static final int CARD_BLOCK_WINDOW_SECONDS = 3600;
    private static final int API_REQUEST_LIMIT = 100;
    private static final int API_REQUEST_WINDOW_SECONDS = 60;

    public enum RateLimitType {
        LOGIN_ATTEMPT,
        TRANSFER_OPERATION,
        CARD_BLOCK,
        API_REQUEST
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private Instant windowStart;

        RequestCounter() {
            this.windowStart = Instant.now();
        }

        void increment() {
            count.incrementAndGet();
        }

        boolean isExpired(int windowSeconds) {
            return Instant.now().minusSeconds(windowSeconds).isAfter(windowStart);
        }

        void resetIfExpired(int windowSeconds) {
            if (isExpired(windowSeconds)) {
                count.set(0);
                windowStart = Instant.now();
            }
        }

        int getCount() {
            return count.get();
        }

        // Метод для проверки и инкремента в одном атомарном действии
        boolean tryIncrement(int limit, int windowSeconds) {
            resetIfExpired(windowSeconds);

            // Атомарно проверяем и инкрементируем
            while (true) {
                int current = count.get();
                if (current >= limit) {
                    return false; // Лимит превышен
                }
                if (count.compareAndSet(current, current + 1)) {
                    return true; // Успешно инкрементировали
                }
                // CAS неудачен, пробуем снова
            }
        }
    }

    private final Map<String, RequestCounter> loginAttemptCounters = new ConcurrentHashMap<>();
    private final Map<String, RequestCounter> transferCounters = new ConcurrentHashMap<>();
    private final Map<String, RequestCounter> cardBlockCounters = new ConcurrentHashMap<>();
    private final Map<String, RequestCounter> apiRequestCounters = new ConcurrentHashMap<>();

    public boolean isRateLimited(String key, RateLimitType type) {
        if (!rateLimitEnabled || key == null) {
            return false;
        }

        Map<String, RequestCounter> counters = getCountersMap(type);
        RequestCounter counter = counters.get(key);

        if (counter == null) {
            return false; // Нет записей - не ограничен
        }

        int windowSeconds = getWindowSeconds(type);
        int limit = getLimit(type);

        counter.resetIfExpired(windowSeconds);
        return counter.getCount() >= limit;
    }

    public boolean tryRecordRequest(String key, RateLimitType type) {
        if (!rateLimitEnabled || key == null) {
            return true; // Всегда успешно, если ограничения выключены
        }

        Map<String, RequestCounter> counters = getCountersMap(type);
        int windowSeconds = getWindowSeconds(type);
        int limit = getLimit(type);

        RequestCounter counter = counters.computeIfAbsent(key, k -> new RequestCounter());
        return counter.tryIncrement(limit, windowSeconds);
    }

    // Старый метод для обратной совместимости
    public void recordRequest(String key, RateLimitType type) {
        tryRecordRequest(key, type);
    }

    private Map<String, RequestCounter> getCountersMap(RateLimitType type) {
        return switch (type) {
            case LOGIN_ATTEMPT -> loginAttemptCounters;
            case TRANSFER_OPERATION -> transferCounters;
            case CARD_BLOCK -> cardBlockCounters;
            case API_REQUEST -> apiRequestCounters;
        };
    }

    private int getLimit(RateLimitType type) {
        return switch (type) {
            case LOGIN_ATTEMPT -> LOGIN_ATTEMPT_LIMIT;
            case TRANSFER_OPERATION -> TRANSFER_LIMIT;
            case CARD_BLOCK -> CARD_BLOCK_LIMIT;
            case API_REQUEST -> API_REQUEST_LIMIT;
        };
    }

    private int getWindowSeconds(RateLimitType type) {
        return switch (type) {
            case LOGIN_ATTEMPT -> LOGIN_ATTEMPT_WINDOW_SECONDS;
            case TRANSFER_OPERATION -> TRANSFER_WINDOW_SECONDS;
            case CARD_BLOCK -> CARD_BLOCK_WINDOW_SECONDS;
            case API_REQUEST -> API_REQUEST_WINDOW_SECONDS;
        };
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredCounters() {
        if (!rateLimitEnabled) return;

        cleanupMap(loginAttemptCounters, LOGIN_ATTEMPT_WINDOW_SECONDS);
        cleanupMap(transferCounters, TRANSFER_WINDOW_SECONDS);
        cleanupMap(cardBlockCounters, CARD_BLOCK_WINDOW_SECONDS);
        cleanupMap(apiRequestCounters, API_REQUEST_WINDOW_SECONDS);
    }

    private void cleanupMap(Map<String, RequestCounter> counters, int windowSeconds) {
        counters.entrySet().removeIf(entry ->
                entry.getValue().isExpired(windowSeconds)
        );
    }
}
