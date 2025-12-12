package com.bank.event.handler;

import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.event.CardBlockRequestedEvent;
import com.bank.event.TransferCompletedEvent;
import com.bank.event.UserActivityEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @InjectMocks
    private NotificationEventHandler notificationEventHandler;

    @Test
    void handleCardBlockRequest_ShouldLogNotification() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        Card card = Card.builder()
                .id(1L)
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();

        CardBlockRequestedEvent event = new CardBlockRequestedEvent(this, card, "Test reason");

        // When & Then - должен выполниться без ошибок
        assertDoesNotThrow(() -> notificationEventHandler.handleCardBlockRequest(event));
    }

    @Test
    void handleTransferCompleted_ShouldLogNotification() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        Card fromCard = Card.builder()
                .id(1L)
                .cardHolder("FROM USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();

        Card toCard = Card.builder()
                .id(2L)
                .cardHolder("TO USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("500.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();

        BigDecimal amount = new BigDecimal("100.00");
        TransferCompletedEvent event = new TransferCompletedEvent(this, null, fromCard, toCard, amount);

        // When & Then - должен выполниться без ошибок
        assertDoesNotThrow(() -> notificationEventHandler.handleTransferCompleted(event));
    }

    @Test
    void handleUserActivity_ShouldLogNotification() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        UserActivityEvent event = new UserActivityEvent(this, user, "LOGIN", "User logged in", "127.0.0.1");

        // When & Then - должен выполниться без ошибок
        assertDoesNotThrow(() -> notificationEventHandler.handleUserActivity(event));
    }

    @Test
    void handleUserActivity_FailedLogin_ShouldLogWarning() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        UserActivityEvent event = new UserActivityEvent(this, user, "FAILED_LOGIN", "Failed login attempt", "127.0.0.1");

        // When & Then - должен выполниться без ошибок
        assertDoesNotThrow(() -> notificationEventHandler.handleUserActivity(event));
    }
}
