package com.bank.event.handler;

import com.bank.entity.Card;
import com.bank.event.CardBlockRequestedEvent;
import com.bank.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventHandler {

    private final CardRepository cardRepository;

    @Async
    @EventListener
    public void handleCardBlockRequest(CardBlockRequestedEvent event) {
        Card card = event.getCard();
        String reason = event.getReason();

        log.info("🔄 Processing card block request: Card {} for user {}, Reason: {}",
                card.getId(), card.getUser().getUsername(), reason);

        // Здесь можно добавить интеграцию с:
        // - Email сервисом для уведомления администраторов
        // - Системой тикетов
        // - Slack/Teams уведомлениями

        log.info("✅ Card block request processed for card: {}", card.getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCardBlockRequestTransactional(CardBlockRequestedEvent event) {
        // Обработка после коммита транзакции
        log.info("📧 Sending notifications for card block request: {}", event.getCard().getId());
    }
}
