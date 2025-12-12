package com.bank.repository;

import com.bank.entity.Card;
import com.bank.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Card activeCard;
    private Card expiredCard;
    private Card blockedCard;

    @BeforeEach
    void setUp() {
        // Очистка базы
        entityManager.getEntityManager().createQuery("DELETE FROM Card").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
        entityManager.clear();

        testUser = User.builder()
                .username("testuser_" + System.currentTimeMillis())
                .password("password")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
        entityManager.persist(testUser);

        activeCard = Card.builder()
                .cardNumber("1111222233334444")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(testUser)
                .blockRequested(false)
                .build();

        expiredCard = Card.builder()
                .cardNumber("2222333344445555")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().minusDays(1))
                .balance(new BigDecimal("500.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(testUser)
                .blockRequested(false)
                .build();

        blockedCard = Card.builder()
                .cardNumber("3333444455556666")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("0.00"))
                .status(Card.CardStatus.BLOCKED)
                .user(testUser)
                .blockRequested(true)
                .build();

        entityManager.persist(activeCard);
        entityManager.persist(expiredCard);
        entityManager.persist(blockedCard);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUser_ShouldReturnUserCards() {
        Page<Card> result = cardRepository.findByUser(testUser, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(card -> card.getUser().getId().equals(testUser.getId())));
    }

    @Test
    void findByUserAndSearch_ShouldFilterByCardHolderOrId() {
        Page<Card> result = cardRepository.findByUserAndSearch(
                testUser,
                "TEST USER",
                PageRequest.of(0, 10)
        );

        assertEquals(3, result.getTotalElements());
    }

    @Test
    void findByUserAndStatus_ShouldReturnFilteredCards() {
        List<Card> activeCards = cardRepository.findByUserAndStatus(testUser, Card.CardStatus.ACTIVE);
        List<Card> blockedCards = cardRepository.findByUserAndStatus(testUser, Card.CardStatus.BLOCKED);

        assertEquals(2, activeCards.size());
        assertEquals(1, blockedCards.size());
        assertTrue(activeCards.stream().allMatch(c -> c.getStatus() == Card.CardStatus.ACTIVE));
    }

    @Test
    void findByStatusAndExpiryDateBefore_ShouldFindExpiredCards() {
        List<Card> expiredCards = cardRepository.findByStatusAndExpiryDateBefore(
                Card.CardStatus.ACTIVE,
                LocalDate.now()
        );

        assertFalse(expiredCards.isEmpty());
        assertTrue(expiredCards.stream()
                .anyMatch(card -> card.getCardNumber().equals("2222333344445555")));
    }

    @Test
    void findActiveUserCards_ShouldReturnActiveNonExpiredCards() {
        List<Card> activeCards = cardRepository.findActiveUserCards(testUser);

        assertEquals(1, activeCards.size());
        assertEquals("1111222233334444", activeCards.get(0).getCardNumber());
    }

    @Test
    void findByIdAndUser_CardExistsAndOwned_ShouldReturnCard() {
        Optional<Card> result = cardRepository.findByIdAndUser(activeCard.getId(), testUser);

        assertTrue(result.isPresent());
        assertEquals(activeCard.getId(), result.get().getId());
    }

    @Test
    void findByIdAndUser_CardNotOwned_ShouldReturnEmpty() {
        User anotherUser = User.builder()
                .username("other_" + System.currentTimeMillis())
                .password("pass")
                .email("other@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
        entityManager.persist(anotherUser);
        entityManager.flush();

        Optional<Card> result = cardRepository.findByIdAndUser(activeCard.getId(), anotherUser);

        assertFalse(result.isPresent());
    }

    @Test
    void existsByCardNumber_CardExists_ShouldReturnTrue() {
        assertTrue(cardRepository.existsByCardNumber("1111222233334444"));
    }

    @Test
    void existsByCardNumber_CardNotExists_ShouldReturnFalse() {
        assertFalse(cardRepository.existsByCardNumber("9999999999999999"));
    }

    @Test
    void existsByIdAndUserId_ShouldReturnCorrectBoolean() {
        assertTrue(cardRepository.existsByIdAndUserId(activeCard.getId(), testUser.getId()));
        assertFalse(cardRepository.existsByIdAndUserId(999L, testUser.getId()));
    }

    @Test
    void findByBlockRequestedTrue_ShouldReturnCardsRequestingBlock() {
        List<Card> blockRequested = cardRepository.findByBlockRequestedTrue();

        assertEquals(1, blockRequested.size());
        assertEquals("3333444455556666", blockRequested.get(0).getCardNumber());
    }

    @Test
    void findWithFilters_ShouldFilterByStatusAndUserId() {
        Page<Card> activeCards = cardRepository.findWithFilters(
                Card.CardStatus.ACTIVE,
                testUser.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(2, activeCards.getTotalElements());

        Page<Card> allUserCards = cardRepository.findWithFilters(
                null,
                testUser.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(3, allUserCards.getTotalElements());
    }

    @Test
    void findExpiredCards_ShouldReturnExpiredCards() {
        Page<Card> expiredCards = cardRepository.findExpiredCards(PageRequest.of(0, 10));

        assertEquals(1, expiredCards.getTotalElements());
        assertEquals("2222333344445555", expiredCards.getContent().get(0).getCardNumber());
    }

    @Test
    void findActiveNonExpiredCards_ShouldReturnActiveNonExpiredCards() {
        Page<Card> activeNonExpired = cardRepository.findActiveNonExpiredCards(PageRequest.of(0, 10));

        assertEquals(1, activeNonExpired.getTotalElements());
        assertEquals("1111222233334444", activeNonExpired.getContent().get(0).getCardNumber());
    }

    @Test
    void countActiveCardsByUserId_ShouldReturnCorrectCount() {
        long count = cardRepository.countActiveCardsByUserId(testUser.getId());

        assertEquals(2, count);
    }

    @Test
    void countByUserId_ShouldReturnTotalUserCards() {
        long count = cardRepository.countByUserId(testUser.getId());

        assertEquals(3, count);
    }
}
