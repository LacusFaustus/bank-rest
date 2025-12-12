package com.bank.controller;

import com.bank.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/admin/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Long>> getMetrics() {
        ConcurrentHashMap<String, AtomicLong> metrics = monitoringService.getAllMetrics();
        Map<String, Long> result = new HashMap<>();

        if (metrics != null) {
            metrics.forEach((key, value) -> result.put(key, value.get()));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double usagePercent = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;

        Map<String, Object> memoryUsage = new HashMap<>();
        memoryUsage.put("usedMemoryMB", usedMemory / (1024 * 1024));
        memoryUsage.put("maxMemoryMB", maxMemory / (1024 * 1024));
        memoryUsage.put("usagePercent", String.format("%.2f%%", usagePercent));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toEpochMilli());
        response.put("memoryUsage", memoryUsage);

        return ResponseEntity.ok(response);
    }
}
