package com.bank.controller;

import com.bank.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/admin/monitoring")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Long>> getMetrics() {
        ConcurrentHashMap<String, Long> metrics = new ConcurrentHashMap<>();
        monitoringService.getAllMetrics().forEach((key, value) -> metrics.put(key, value.get()));

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "memoryUsage", getMemoryUsage()
        );

        return ResponseEntity.ok(health);
    }

    private Map<String, Object> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        return Map.of(
                "usedMemoryMB", usedMemory / (1024 * 1024),
                "maxMemoryMB", maxMemory / (1024 * 1024),
                "usagePercent", String.format("%.2f%%", memoryUsagePercent)
        );
    }
}
