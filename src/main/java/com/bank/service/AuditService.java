package com.bank.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuditService {

    // Существующие методы
    public void logSecurityEvent(String eventType, String description, boolean success, HttpServletRequest request) {
        logEvent("SECURITY", eventType, description, success, null, null, request);
    }

    public void logTransactionEvent(String eventType, String description, double amount,
                                    String fromAccount, String toAccount, HttpServletRequest request) {
        logEvent("TRANSACTION", eventType, description, true, fromAccount, toAccount, request);
    }

    // Новый универсальный метод для обратной совместимости
    public void logActivity(String eventType, String description, boolean success,
                            String fromAccount, String toAccount, HttpServletRequest request) {
        logEvent("ACTIVITY", eventType, description, success, fromAccount, toAccount, request);
    }

    private void logEvent(String logType, String eventType, String description, boolean success,
                          String fromAccount, String toAccount, HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request);
            String userAgent = getUserAgent(request);

            String maskedFrom = maskAccountNumber(fromAccount);
            String maskedTo = maskAccountNumber(toAccount);

            log.info("[{} AUDIT] Event: {}, Success: {}, From: {}, To: {}, IP: {}, User-Agent: {}, Description: {}, Time: {}",
                    logType, eventType, success, maskedFrom, maskedTo, clientIp, userAgent,
                    description, LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error logging {} event", logType.toLowerCase(), e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return "****";
        }
        return accountNumber.substring(0, 4) + "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
