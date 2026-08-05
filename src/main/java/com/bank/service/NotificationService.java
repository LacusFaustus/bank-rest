package com.bank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Async
    public void sendBlockRequestNotification(Long cardId, String username) {
        log.info("📧 Sending block request notification: Card {} by user {}", cardId, username);
        // Integration with email/SMS/notification services
    }

    @Async
    public void sendTransferNotification(Long fromCardId, Long toCardId, String amount) {
        log.info("📧 Sending transfer notification: {} from {} to {}", amount, fromCardId, toCardId);
    }

    @Async
    public void sendSecurityAlert(String username, String ipAddress, String event) {
        log.warn("🚨 Security alert: User {} from IP {} - {}", username, ipAddress, event);
    }
}
