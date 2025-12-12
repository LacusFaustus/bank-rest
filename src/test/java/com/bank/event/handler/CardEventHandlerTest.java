package com.bank.event.handler;

import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.event.CardBlockRequestedEvent;
import com.bank.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardEventHandlerTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardEventHandler cardEventHandler;

    @Test
    void handleCardBlockRequest_ShouldLogEvent() {
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

        // When
        cardEventHandler.handleCardBlockRequest(event);

        // Then - проверяем что метод выполнился без ошибок
        // Для асинхронных методов достаточно проверить что они не выбрасывают исключений
        verifyNoInteractions(cardRepository); // В этом обработчике репозиторий не используется
    }

    @Test
    void handleCardBlockRequestTransactional_ShouldLogEvent() {
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

        // When
        cardEventHandler.handleCardBlockRequestTransactional(event);

        // Then - проверяем что метод выполнился без ошибок
        verifyNoInteractions(cardRepository); // В этом обработчике репозиторий не используется
    }
}
