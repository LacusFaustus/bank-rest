package com.bank.controller;

import com.bank.dto.PaginatedResponse;
import com.bank.entity.AuditLog;
import com.bank.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private final AuditLogRepository auditLogRepository;

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

        Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(
                username, actionType, success, startDate, endDate, PageRequest.of(page, size));

        return ResponseEntity.ok(PaginatedResponse.of(auditLogs));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<PaginatedResponse<AuditLog>> getUserAuditLogs(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<AuditLog> auditLogs = auditLogRepository.findByUsername(
                username, PageRequest.of(page, size));

        return ResponseEntity.ok(PaginatedResponse.of(auditLogs));
    }
}
