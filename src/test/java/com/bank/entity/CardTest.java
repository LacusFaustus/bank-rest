package com.bank.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void cardStatusChecks_ShouldWorkCorrectly() {
        // Given
        Card activeCard = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusDays(1))
                .build();

        Card expiredCard = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        Card blockedCard = Card.builder()
                .status(Card.CardStatus.BLOCKED)
                .expiryDate(LocalDate.now().plusDays(1))
                .build();

        // Then
        assertTrue(activeCard.isActive());
        assertFalse(expiredCard.isActive());
        assertFalse(blockedCard.isActive());
        assertTrue(expiredCard.isExpired());
        assertTrue(blockedCard.isBlocked());
    }

    @Test
    void cardBuilder_ShouldSetDefaultValues() {
        // When
        Card card = Card.builder()
                .cardNumber("1234567890123456")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .build();

        // Then
        assertNotNull(card);
        assertFalse(card.getBlockRequested());
    }

    @Test
    void cardEqualsAndHashCode_ShouldWorkCorrectly() {
        // Given
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card3 = Card.builder()
                .id(2L)
                .cardHolder("OTHER USER")
                .expiryDate(LocalDate.of(2024, 6, 30))
                .build();

        // Then
        assertEquals(card1, card2);
        assertNotEquals(card1, card3);
        assertEquals(card1.hashCode(), card2.hashCode());
    }
}
