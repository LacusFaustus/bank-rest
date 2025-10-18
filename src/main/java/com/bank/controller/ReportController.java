package com.bank.controller;

import com.bank.service.TransactionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TransactionReportService transactionReportService;

    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDailyReport() {
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@AuthenticationPrincipal com.bank.entity.User user) {
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "OK", "service", "Bank REST API"));
    }
}
