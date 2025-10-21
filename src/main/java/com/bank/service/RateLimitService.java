package com.bank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final CacheManager cacheManager;

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int MAX_LOGIN_ATTEMPTS_PER_HOUR = 5;
    private static final int MAX_TRANSFERS_PER_DAY = 10;

    public boolean isRateLimited(String key, RateLimitType type) {
        String cacheKey = type + "_" + key;
        Cache cache = cacheManager.getCache("rateLimit");

        if (cache == null) {
            return false;
        }

        RateLimitInfo rateLimitInfo = cache.get(cacheKey, RateLimitInfo.class);
        if (rateLimitInfo == null) {
            rateLimitInfo = new RateLimitInfo();
            cache.put(cacheKey, rateLimitInfo);
        }

        return rateLimitInfo.isRateLimited(type);
    }

    public void recordRequest(String key, RateLimitType type) {
        String cacheKey = type + "_" + key;
        Cache cache = cacheManager.getCache("rateLimit");

        if (cache == null) {
            return;
        }

        RateLimitInfo rateLimitInfo = cache.get(cacheKey, RateLimitInfo.class);
        if (rateLimitInfo == null) {
            rateLimitInfo = new RateLimitInfo();
        }

        rateLimitInfo.recordRequest(type);
        cache.put(cacheKey, rateLimitInfo);
    }

    public enum RateLimitType {
        API_REQUEST,
        LOGIN_ATTEMPT,
        TRANSFER_OPERATION
    }

    private static class RateLimitInfo {
        private final AtomicInteger requestsLastMinute = new AtomicInteger(0);
        private final AtomicInteger loginAttemptsLastHour = new AtomicInteger(0);
        private final AtomicInteger transfersLastDay = new AtomicInteger(0);
        private LocalDateTime minuteWindowStart = LocalDateTime.now();
        private LocalDateTime hourWindowStart = LocalDateTime.now();
        private LocalDateTime dayWindowStart = LocalDateTime.now();

        public synchronized boolean isRateLimited(RateLimitType type) {
            resetCountersIfNeeded();

            switch (type) {
                case API_REQUEST:
                    return requestsLastMinute.get() >= MAX_REQUESTS_PER_MINUTE;
                case LOGIN_ATTEMPT:
                    return loginAttemptsLastHour.get() >= MAX_LOGIN_ATTEMPTS_PER_HOUR;
                case TRANSFER_OPERATION:
                    return transfersLastDay.get() >= MAX_TRANSFERS_PER_DAY;
                default:
                    return false;
            }
        }

        public synchronized void recordRequest(RateLimitType type) {
            resetCountersIfNeeded();

            switch (type) {
                case API_REQUEST:
                    requestsLastMinute.incrementAndGet();
                    break;
                case LOGIN_ATTEMPT:
                    loginAttemptsLastHour.incrementAndGet();
                    break;
                case TRANSFER_OPERATION:
                    transfersLastDay.incrementAndGet();
                    break;
            }
        }

        private void resetCountersIfNeeded() {
            LocalDateTime now = LocalDateTime.now();

            if (now.minusMinutes(1).isAfter(minuteWindowStart)) {
                requestsLastMinute.set(0);
                minuteWindowStart = now;
            }

            if (now.minusHours(1).isAfter(hourWindowStart)) {
                loginAttemptsLastHour.set(0);
                hourWindowStart = now;
            }

            if (now.minusDays(1).isAfter(dayWindowStart)) {
                transfersLastDay.set(0);
                dayWindowStart = now;
            }
        }
    }
}
