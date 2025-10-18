package com.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class MonitoringService {

    private final ConcurrentHashMap<String, AtomicLong> metrics = new ConcurrentHashMap<>();

    public MonitoringService() {
        // Инициализация метрик
        metrics.put("login.success", new AtomicLong(0));
        metrics.put("login.failure", new AtomicLong(0));
        metrics.put("transfer.completed", new AtomicLong(0));
        metrics.put("card.block.requests", new AtomicLong(0));
        metrics.put("api.requests", new AtomicLong(0));
    }

    public void recordSuccessfulLogin(String username) {
        metrics.get("login.success").incrementAndGet();
        log.info("✅ Successful login for user: {}", username);
    }

    public void recordFailedLogin(String username) {
        metrics.get("login.failure").incrementAndGet();
        log.warn("❌ Failed login attempt for user: {}", username);
    }

    public void recordTransfer(String fromUser, String toUser, BigDecimal amount) {
        metrics.get("transfer.completed").incrementAndGet();
        log.info("💰 Transfer completed: {} from {} to {}", amount, fromUser, toUser);
    }

    public void recordCardBlockRequest(Long cardId, String username) {
        metrics.get("card.block.requests").incrementAndGet();
        log.info("🚫 Card block requested: card {} by user {}", cardId, username);
    }

    public void recordApiRequest() {
        metrics.get("api.requests").incrementAndGet();
    }

    public long getMetric(String metricName) {
        AtomicLong metric = metrics.get(metricName);
        return metric != null ? metric.get() : 0;
    }

    public ConcurrentHashMap<String, AtomicLong> getAllMetrics() {
        return new ConcurrentHashMap<>(metrics);
    }

    public void recordSystemHealth() {
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory * 100;

        log.debug("💾 Memory usage: {}/{} MB ({:.2f}%)",
                usedMemory / (1024 * 1024),
                maxMemory / (1024 * 1024),
                memoryUsage);
    }
}
