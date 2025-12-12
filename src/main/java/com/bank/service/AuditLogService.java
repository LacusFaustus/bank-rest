package com.bank.service;

import com.bank.dto.PaginatedResponse;
import com.bank.entity.AuditLog;
import com.bank.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public PaginatedResponse<AuditLog> getAuditLogsWithFilters(
            String username,
            String actionType,
            Boolean success,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size) {

        Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(
                username, actionType, success, startDate, endDate,
                PageRequest.of(page, size));

        return PaginatedResponse.of(auditLogs);
    }

    public PaginatedResponse<AuditLog> getUserAuditLogs(
            String username,
            int page,
            int size) {

        Page<AuditLog> auditLogs = auditLogRepository.findByUsername(
                username, PageRequest.of(page, size));

        return PaginatedResponse.of(auditLogs);
    }
}
