package com.bank.repository;

import com.bank.entity.Card;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        entityManager.getEntityManager().createQuery("DELETE FROM Transaction").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Card").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
    }

    @Test
    void simpleTransactionTest() {
        // Создаем пользователей с уникальными username
        User user1 = User.builder()
                .username("trans_user1")
                .password("pass1")
                .email("trans1@test.com")
                .role(User.Role.ROLE_USER)
                .build();

        User user2 = User.builder()
                .username("trans_user2")
                .password("pass2")
                .email("trans2@test.com")
                .role(User.Role.ROLE_USER)
                .build();

        entityManager.persist(user1);
        entityManager.persist(user2);

        // Создаем карты
        Card card1 = Card.builder()
                .cardNumber("1111222233334444")
                .cardHolder("USER ONE")
                .balance(new BigDecimal("1000.00"))
                .expiryDate(LocalDateTime.now().plusYears(1).toLocalDate())
                .status(Card.CardStatus.ACTIVE)
                .user(user1)
                .build();

        Card card2 = Card.builder()
                .cardNumber("5555666677778888")
                .cardHolder("USER TWO")
                .balance(new BigDecimal("500.00"))
                .expiryDate(LocalDateTime.now().plusYears(1).toLocalDate())
                .status(Card.CardStatus.ACTIVE)
                .user(user2)
                .build();

        entityManager.persist(card1);
        entityManager.persist(card2);
        entityManager.flush();

        // Создаем транзакцию
        Transaction transaction = Transaction.builder()
                .fromCard(card1)
                .toCard(card2)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now())
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        entityManager.persist(transaction);
        entityManager.flush();
        entityManager.clear();

        // Проверяем
        List<Transaction> allTransactions = transactionRepository.findAll();
        assertEquals(1, allTransactions.size());

        List<Transaction> user1Transactions = transactionRepository.findByUserId(user1.getId());
        assertEquals(1, user1Transactions.size());

        List<Transaction> user2Transactions = transactionRepository.findByUserId(user2.getId());
        assertEquals(1, user2Transactions.size());
    }

    @Test
    void findByUserId_ShouldReturnUserTransactions() {
        // Создаем уникальных пользователей
        User user1 = createTestUser("user1_unique");
        User user2 = createTestUser("user2_unique");

        Card card1 = createTestCard("1111", user1);
        Card card2 = createTestCard("2222", user2);
        Card card3 = createTestCard("3333", user1);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(card1);
        entityManager.persist(card2);
        entityManager.persist(card3);
        entityManager.flush();

        // Транзакция 1: user1 -> user2
        Transaction t1 = Transaction.builder()
                .fromCard(card1)
                .toCard(card2)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now().minusDays(1))
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        // Транзакция 2: user2 -> user1
        Transaction t2 = Transaction.builder()
                .fromCard(card2)
                .toCard(card1)
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDateTime.now())
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        // Транзакция 3: между картами user1
        Transaction t3 = Transaction.builder()
                .fromCard(card1)
                .toCard(card3)
                .amount(new BigDecimal("25.00"))
                .transactionDate(LocalDateTime.now())
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();
        entityManager.clear();

        // Проверяем
        List<Transaction> user1Transactions = transactionRepository.findByUserId(user1.getId());
        assertEquals(3, user1Transactions.size());

        List<Transaction> user2Transactions = transactionRepository.findByUserId(user2.getId());
        assertEquals(2, user2Transactions.size());
    }

    private User createTestUser(String username) {
        return User.builder()
                .username(username + "_" + System.currentTimeMillis())
                .password("password")
                .email(username + "@test.com")
                .role(User.Role.ROLE_USER)
                .build();
    }

    private Card createTestCard(String lastFour, User user) {
        return Card.builder()
                .cardNumber("111122223333" + lastFour)
                .cardHolder(user.getUsername().toUpperCase())
                .balance(new BigDecimal("1000.00"))
                .expiryDate(LocalDateTime.now().plusYears(1).toLocalDate())
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();
    }
}
