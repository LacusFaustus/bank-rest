package com.bank.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CardTest {

    @Test
    void builder_ShouldCreateCardWithAllFields() {
        LocalDate expiryDate = LocalDate.now().plusYears(1);
        User user = User.builder().id(1L).username("testuser").build();

        Card card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardHolder("TEST USER")
                .expiryDate(expiryDate)
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .blockRequested(false)
                .createdAt(LocalDateTime.now())
                .build();

        assertNotNull(card);
        assertEquals(1L, card.getId());
        assertEquals("1234567890123456", card.getCardNumber());
        assertEquals("TEST USER", card.getCardHolder());
        assertEquals(expiryDate, card.getExpiryDate());
        assertEquals(new BigDecimal("1000.00"), card.getBalance());
        assertEquals(Card.CardStatus.ACTIVE, card.getStatus());
        assertEquals(user, card.getUser());
        assertFalse(card.getBlockRequested());
    }

    @Test
    void isActive_ActiveCardNotExpired_ShouldReturnTrue() {
        Card activeCard = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusDays(1))
                .build();

        assertTrue(activeCard.isActive());
    }

    @Test
    void isActive_ActiveCardExpiresToday_ShouldReturnTrue() {
        Card card = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now())
                .build();

        // Согласно текущей реализации, карта действительна до конца дня указанной даты
        assertTrue(card.isActive());
        assertFalse(card.isExpired());
    }

    @Test
    void isActive_BlockedCard_ShouldReturnFalse() {
        Card blockedCard = Card.builder()
                .status(Card.CardStatus.BLOCKED)
                .expiryDate(LocalDate.now().plusDays(1))
                .build();

        assertFalse(blockedCard.isActive());
    }

    @Test
    void isActive_ExpiredCard_ShouldReturnFalse() {
        Card expiredCard = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        assertFalse(expiredCard.isActive());
        assertTrue(expiredCard.isExpired());
    }

    static Stream<Arguments> expiredDateProvider() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                arguments(today.plusDays(1), false),
                arguments(today, false),           // сегодня - не считается просроченной
                arguments(today.minusDays(1), true),
                arguments(today.minusYears(1), true)
        );
    }

    @ParameterizedTest
    @MethodSource("expiredDateProvider")
    void isExpired_ShouldReturnCorrectValue(LocalDate expiryDate, boolean expected) {
        Card card = Card.builder()
                .expiryDate(expiryDate)
                .build();

        assertEquals(expected, card.isExpired());
    }

    @Test
    void isExpired_FutureExpiryDate_ShouldReturnFalse() {
        Card futureCard = Card.builder()
                .expiryDate(LocalDate.now().plusDays(30))
                .build();

        assertFalse(futureCard.isExpired());
    }

    @Test
    void isExpired_PastExpiryDate_ShouldReturnTrue() {
        Card pastCard = Card.builder()
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        assertTrue(pastCard.isExpired());
    }

    @Test
    void isExpired_TodayExpiryDate_ShouldReturnFalse() {
        Card todayCard = Card.builder()
                .expiryDate(LocalDate.now())
                .build();

        assertFalse(todayCard.isExpired());
    }

    @ParameterizedTest
    @EnumSource(Card.CardStatus.class)
    void isBlocked_ShouldReturnCorrectValue(Card.CardStatus status) {
        Card card = Card.builder()
                .status(status)
                .build();

        boolean expected = status == Card.CardStatus.BLOCKED;
        assertEquals(expected, card.isBlocked());
    }

    @Test
    void isBlocked_BlockedStatus_ShouldReturnTrue() {
        Card blockedCard = Card.builder()
                .status(Card.CardStatus.BLOCKED)
                .build();

        assertTrue(blockedCard.isBlocked());
    }

    @Test
    void isBlocked_ActiveStatus_ShouldReturnFalse() {
        Card activeCard = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .build();

        assertFalse(activeCard.isBlocked());
    }

    @Test
    void cardStatusEnum_ShouldHaveAllValues() {
        Card.CardStatus[] values = Card.CardStatus.values();

        assertEquals(3, values.length);
        assertArrayEquals(new Card.CardStatus[]{
                Card.CardStatus.ACTIVE,
                Card.CardStatus.BLOCKED,
                Card.CardStatus.EXPIRED
        }, values);
    }

    @Test
    void cardStatus_ValueOfShouldWork() {
        assertEquals(Card.CardStatus.ACTIVE, Card.CardStatus.valueOf("ACTIVE"));
        assertEquals(Card.CardStatus.BLOCKED, Card.CardStatus.valueOf("BLOCKED"));
        assertEquals(Card.CardStatus.EXPIRED, Card.CardStatus.valueOf("EXPIRED"));
    }

    @Test
    void cardStatus_ValueOfInvalid_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            Card.CardStatus.valueOf("INVALID");
        });
    }

    @Test
    void builder_ShouldSetDefaultBlockRequestedToFalse() {
        Card card = Card.builder()
                .cardNumber("1234567890123456")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        assertNotNull(card);
        assertFalse(card.getBlockRequested());
    }

    @Test
    void equalsAndHashCode_ShouldWorkWithIncludedFields() {
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.BLOCKED)
                .blockRequested(true)
                .build();

        Card card3 = Card.builder()
                .id(2L)
                .cardHolder("JANE SMITH")
                .expiryDate(LocalDate.of(2024, 6, 30))
                .build();

        assertEquals(card1, card2);
        assertNotEquals(card1, card3);
        assertEquals(card1.hashCode(), card2.hashCode());
        assertNotEquals(card1.hashCode(), card3.hashCode());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "ANOTHER HOLDER"})
    void equals_WithDifferentCardHolders_ShouldBeDifferent(String cardHolder) {
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder("REFERENCE CARD")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder(cardHolder)
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        if ("REFERENCE CARD".equals(cardHolder)) {
            assertEquals(card1, card2);
        } else {
            assertNotEquals(card1, card2);
        }
    }

    @Test
    void equals_WithDifferentExpiryDates_ShouldBeDifferent() {
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.of(2026, 12, 31))
                .build();

        assertNotEquals(card1, card2);
    }

    @Test
    void equals_WithDifferentIds_ShouldBeDifferent() {
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card2 = Card.builder()
                .id(2L)
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        assertNotEquals(card1, card2);
    }

    @Test
    void equals_ShouldHandleNullAndDifferentClass() {
        Card card = Card.builder()
                .id(1L)
                .cardHolder("TEST")
                .expiryDate(LocalDate.now())
                .build();

        assertNotEquals(null, card);
        assertNotEquals("string", card);
        assertEquals(card, card);
    }

    @Test
    void toString_ShouldNotExposeCardNumber() {
        Card card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardHolder("JOHN DOE")
                .balance(new BigDecimal("9999.99"))
                .expiryDate(LocalDate.of(2025, 12, 31))
                .status(Card.CardStatus.ACTIVE)
                .build();

        String toString = card.toString();

        assertNotNull(toString);
        assertFalse(toString.contains("1234567890123456"),
                "Card number should not be exposed in toString()");
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("cardHolder=JOHN DOE"));
    }

    @Test
    void inSet_ShouldRespectEqualsAndHashCode() {
        Set<Card> cardSet = new HashSet<>();

        Card card1 = Card.builder()
                .id(1L)
                .cardHolder("USER ONE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder("USER ONE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        Card card3 = Card.builder()
                .id(2L)
                .cardHolder("USER TWO")
                .expiryDate(LocalDate.of(2024, 6, 30))
                .build();

        cardSet.add(card1);
        cardSet.add(card2);
        cardSet.add(card3);

        assertEquals(2, cardSet.size());
        assertTrue(cardSet.contains(card1));
        assertTrue(cardSet.contains(card2));
        assertTrue(cardSet.contains(card3));
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyCard() {
        Card card = new Card();

        assertNotNull(card);
        assertNull(card.getId());
        assertNull(card.getCardNumber());
        assertNull(card.getCardHolder());
        assertNull(card.getExpiryDate());
        assertNull(card.getBalance());
        assertNull(card.getStatus());
        assertNull(card.getUser());
        assertNull(card.getCreatedAt());
        assertFalse(card.getBlockRequested());
    }

    @Test
    void allArgsConstructor_ShouldCreateCompleteCard() {
        LocalDate expiryDate = LocalDate.of(2025, 12, 31);
        LocalDateTime createdAt = LocalDateTime.now();
        User user = User.builder().id(1L).build();

        Card card = new Card(
                1L,
                "1234567890123456",
                "JOHN DOE",
                expiryDate,
                new BigDecimal("1000.00"),
                Card.CardStatus.ACTIVE,
                user,
                createdAt,
                false
        );

        assertNotNull(card);
        assertEquals(1L, card.getId());
        assertEquals("1234567890123456", card.getCardNumber());
        assertEquals("JOHN DOE", card.getCardHolder());
        assertEquals(expiryDate, card.getExpiryDate());
        assertEquals(new BigDecimal("1000.00"), card.getBalance());
        assertEquals(Card.CardStatus.ACTIVE, card.getStatus());
        assertEquals(user, card.getUser());
        assertEquals(createdAt, card.getCreatedAt());
        assertFalse(card.getBlockRequested());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Card card = new Card();
        LocalDate expiryDate = LocalDate.of(2025, 12, 31);
        LocalDateTime createdAt = LocalDateTime.now();
        User user = User.builder().id(1L).build();

        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setCardHolder("JANE SMITH");
        card.setExpiryDate(expiryDate);
        card.setBalance(new BigDecimal("1500.50"));
        card.setStatus(Card.CardStatus.BLOCKED);
        card.setUser(user);
        card.setCreatedAt(createdAt);
        card.setBlockRequested(true);

        assertEquals(1L, card.getId());
        assertEquals("1234567890123456", card.getCardNumber());
        assertEquals("JANE SMITH", card.getCardHolder());
        assertEquals(expiryDate, card.getExpiryDate());
        assertEquals(new BigDecimal("1500.50"), card.getBalance());
        assertEquals(Card.CardStatus.BLOCKED, card.getStatus());
        assertEquals(user, card.getUser());
        assertEquals(createdAt, card.getCreatedAt());
        assertTrue(card.getBlockRequested());
    }

    @Test
    void builder_WithoutBlockRequested_ShouldUseDefaultFalse() {
        Card card = Card.builder()
                .cardNumber("1234567890123456")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("500.00"))
                .status(Card.CardStatus.ACTIVE)
                .build();

        assertNotNull(card);
        assertFalse(card.getBlockRequested());
    }

    @Test
    void builder_WithExplicitBlockRequested_ShouldUseProvidedValue() {
        Card card = Card.builder()
                .cardNumber("1234567890123456")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("500.00"))
                .status(Card.CardStatus.ACTIVE)
                .blockRequested(true)
                .build();

        assertNotNull(card);
        assertTrue(card.getBlockRequested());
    }

    @Test
    void hashCode_Consistency() {
        Card card = Card.builder()
                .id(1L)
                .cardHolder("TEST")
                .expiryDate(LocalDate.now())
                .build();

        int hashCode1 = card.hashCode();
        int hashCode2 = card.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void equals_WithNullId_ShouldHandleGracefully() {
        Card card1 = Card.builder()
                .id(null)
                .cardHolder("HOLDER")
                .expiryDate(LocalDate.now())
                .build();

        Card card2 = Card.builder()
                .id(null)
                .cardHolder("HOLDER")
                .expiryDate(LocalDate.now())
                .build();

        assertEquals(card1, card2);
        assertEquals(card1.hashCode(), card2.hashCode());
    }

    @Test
    void equals_WithNullCardHolder_ShouldHandleGracefully() {
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder(null)
                .expiryDate(LocalDate.now())
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder(null)
                .expiryDate(LocalDate.now())
                .build();

        assertEquals(card1, card2);
        assertEquals(card1.hashCode(), card2.hashCode());
    }

    @Test
    void equals_WithNullExpiryDate_ShouldHandleGracefully() {
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder("HOLDER")
                .expiryDate(null)
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder("HOLDER")
                .expiryDate(null)
                .build();

        assertEquals(card1, card2);
        assertEquals(card1.hashCode(), card2.hashCode());
    }

    @Test
    void equals_WithMixedNullFields_ShouldBeDifferent() {
        Card card1 = Card.builder()
                .id(1L)
                .cardHolder(null)
                .expiryDate(LocalDate.now())
                .build();

        Card card2 = Card.builder()
                .id(1L)
                .cardHolder("HOLDER")
                .expiryDate(null)
                .build();

        assertNotEquals(card1, card2);
    }

    @Test
    void blockRequested_DefaultValueShouldBeFalse() {
        Card card = new Card();
        assertFalse(card.getBlockRequested());
    }

    @Test
    void blockRequested_CanBeSetToNull() {
        Card card = new Card();
        card.setBlockRequested(null);
        assertNull(card.getBlockRequested());
    }

    @Test
    void isActive_WhenExpiredStatus_ShouldReturnFalse() {
        Card card = Card.builder()
                .status(Card.CardStatus.EXPIRED)
                .expiryDate(LocalDate.now().plusDays(1))
                .build();

        assertFalse(card.isActive());
    }

    @Test
    void isActive_WhenBlockedAndNotExpired_ShouldReturnFalse() {
        Card card = Card.builder()
                .status(Card.CardStatus.BLOCKED)
                .expiryDate(LocalDate.now().plusDays(1))
                .build();

        assertFalse(card.isActive());
    }

    @Test
    void isActive_WhenActiveAndExpired_ShouldReturnFalse() {
        Card card = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        assertFalse(card.isActive());
        assertTrue(card.isExpired());
    }

    @Test
    void isActive_WhenActiveAndExpiresToday_ShouldReturnTrue() {
        Card card = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now())
                .build();

        // Карта с датой истечения сегодня считается активной (действительна до конца дня)
        assertTrue(card.isActive());
        assertFalse(card.isExpired());
    }

    @Test
    void isExpired_NullExpiryDate_ShouldThrow() {
        Card card = new Card();
        card.setExpiryDate(null);

        assertThrows(NullPointerException.class, card::isExpired);
    }

    @Test
    void isActive_NullStatus_ShouldReturnFalse() {
        Card card = new Card();
        card.setStatus(null);
        card.setExpiryDate(LocalDate.now().plusDays(1));

        // При сравнении null == CardStatus.ACTIVE возвращается false
        // Правая часть !isExpired() не вычисляется из-за short-circuit evaluation
        assertFalse(card.isActive());
    }

    @Test
    void isBlocked_NullStatus_ShouldReturnFalse() {
        Card card = new Card();
        card.setStatus(null);

        // null == CardStatus.BLOCKED возвращает false
        assertFalse(card.isBlocked());
    }

    @Test
    void isActive_WithNullExpiryDate_ShouldThrow() {
        Card card = Card.builder()
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(null)
                .build();

        // status == CardStatus.ACTIVE = true, поэтому вычисляется !isExpired()
        // isExpired() бросит NPE при expiryDate.isBefore(LocalDate.now())
        assertThrows(NullPointerException.class, card::isActive);
    }

    @Test
    void isActive_WithNullStatusButNotNullExpiryDate_ShouldNotThrow() {
        Card card = new Card();
        card.setStatus(null);
        card.setExpiryDate(LocalDate.now().plusDays(1));

        // status == CardStatus.ACTIVE = false, правая часть не вычисляется
        // поэтому isExpired() не вызывается и исключения нет
        assertFalse(card.isActive());
    }

    @Test
    void isActive_NullStatusAndNullExpiryDate_ShouldReturnFalse() {
        Card card = new Card();
        card.setStatus(null);
        card.setExpiryDate(null);

        // status == CardStatus.ACTIVE = false, правая часть не вычисляется
        // поэтому isExpired() не вызывается и исключения нет
        assertFalse(card.isActive());
    }

    @Test
    void isActive_WithBlockedStatusAndNullExpiryDate_ShouldThrow() {
        Card card = Card.builder()
                .status(Card.CardStatus.BLOCKED)
                .expiryDate(null)
                .build();

        // status == CardStatus.ACTIVE = false, правая часть не вычисляется
        // поэтому isExpired() не вызывается и исключения нет
        assertFalse(card.isActive());
    }

    @Test
    void directIsExpiredCall_WithNullExpiryDate_ShouldThrow() {
        Card card = new Card();
        card.setExpiryDate(null);

        assertThrows(NullPointerException.class, card::isExpired);
    }
}
