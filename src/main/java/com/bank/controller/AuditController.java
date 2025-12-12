package com.bank.controller;

import com.bank.dto.PaginatedResponse;
import com.bank.entity.AuditLog;
import com.bank.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) startDate = LocalDateTime.now().minusDays(7);
        if (endDate == null) endDate = LocalDateTime.now();

        PaginatedResponse<AuditLog> auditLogs = auditLogService.getAuditLogsWithFilters(
                username, actionType, success, startDate, endDate, page, size);

        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<PaginatedResponse<AuditLog>> getUserAuditLogs(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PaginatedResponse<AuditLog> auditLogs = auditLogService.getUserAuditLogs(
                username, page, size);

        return ResponseEntity.ok(auditLogs);
    }
}
