package com.bank.event.handler;

import com.bank.event.CardBlockRequestedEvent;
import com.bank.event.TransferCompletedEvent;
import com.bank.event.UserActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    @Async
    @EventListener
    public void handleCardBlockRequest(CardBlockRequestedEvent event) {
        log.info("🔔 Card block requested notification: Card {}, Reason: {}",
                event.getCard().getId(), event.getReason());

        // Здесь можно интегрировать с:
        // - Email сервисом
        // - SMS уведомлениями
        // - Slack/Teams webhooks
        // - Системой тикетов для администраторов
    }

    @Async
    @EventListener
    public void handleTransferCompleted(TransferCompletedEvent event) {
        log.info("🔔 Transfer completed: {} from card {} to card {}",
                event.getAmount(), event.getFromCard().getId(), event.getToCard().getId());

        // Уведомления пользователю о successful transfer
    }

    @Async
    @EventListener
    public void handleUserActivity(UserActivityEvent event) {
        log.info("🔔 User activity: {} - {} from IP: {}",
                event.getUser().getUsername(), event.getActivityType(), event.getIpAddress());

        // Мониторинг подозрительной активности
        if ("FAILED_LOGIN".equals(event.getActivityType())) {
            log.warn("⚠️ Failed login attempt for user: {} from IP: {}",
                    event.getUser().getUsername(), event.getIpAddress());
        }
    }
}
