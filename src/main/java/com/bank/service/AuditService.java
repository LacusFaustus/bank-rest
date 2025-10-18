package com.bank.service;

import com.bank.entity.AuditLog;
import com.bank.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logActivity(String actionType, String description, boolean success,
                            String resourceId, String requestDetails, HttpServletRequest request) {

        String username = getCurrentUsername();
        String ipAddress = getClientIp(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        AuditLog auditLog = AuditLog.builder()
                .actionType(actionType)
                .description(description)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(success)
                .resourceId(resourceId)
                .requestDetails(requestDetails)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {}", actionType, description);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    public void logSecurityEvent(String actionType, String description, boolean success,
                                 HttpServletRequest request) {
        logActivity(actionType, description, success, null, null, request);
    }

    public void logTransferActivity(Long fromCardId, Long toCardId, BigDecimal amount,
                                    boolean success, String errorMessage, HttpServletRequest request) {
        String description = String.format("Transfer %.2f from card %d to card %d",
                amount, fromCardId, toCardId);
        String requestDetails = String.format("{\"fromCardId\": %d, \"toCardId\": %d, \"amount\": %.2f}",
                fromCardId, toCardId, amount);

        logActivity("TRANSFER", description, success, fromCardId.toString(),
                requestDetails, request);

        if (!success && errorMessage != null) {
            // Дополнительный лог для ошибок
            logActivity("TRANSFER_ERROR", errorMessage, false, fromCardId.toString(),
                    requestDetails, request);
        }
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
