package com.bank.repository;

import com.bank.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RepositoryIntegrationTestContainers {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        // Clear all tables in correct order due to foreign key constraints
        transactionRepository.deleteAll();
        auditLogRepository.deleteAll();
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void fullUserCardTransactionFlow_ShouldWork() {
        LocalDateTime testTime = LocalDateTime.now();

        // Create user
        User user = User.builder()
                .username("integration_user_" + System.currentTimeMillis())
                .password("pass")
                .email("integration_" + System.currentTimeMillis() + "@test.com")
                .role(User.Role.ROLE_USER)
                .build();
        user = userRepository.save(user);

        // Create card for user
        Card card = Card.builder()
                .cardNumber("1234567812345678")
                .cardHolder("INTEGRATION USER")
                .balance(new BigDecimal("1000.00"))
                .expiryDate(testTime.plusYears(1).toLocalDate())
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();
        card = cardRepository.save(card);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .fromCard(card)
                .toCard(card) // Self-transaction for simplicity
                .amount(new BigDecimal("100.00"))
                .transactionDate(testTime)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();
        transaction = transactionRepository.save(transaction);

        // Create audit log
        AuditLog auditLog = AuditLog.builder()
                .username(user.getUsername())
                .actionType("TRANSACTION")
                .description("Made transaction #" + transaction.getId())
                .success(true)
                .timestamp(testTime)
                .ipAddress("127.0.0.1")
                .userAgent("IntegrationTest/1.0")
                .resourceId("transaction-" + transaction.getId())
                .requestDetails("Amount: 100.00")
                .build();
        auditLog = auditLogRepository.save(auditLog);

        // Verify all relationships
        assertNotNull(user.getId());
        assertNotNull(card.getId());
        assertNotNull(transaction.getId());
        assertNotNull(auditLog.getId());

        // Verify user-card relationship
        assertEquals(1, cardRepository.findByUser(user, PageRequest.of(0, 10)).getTotalElements());

        // Verify user transactions
        assertEquals(1, transactionRepository.findByUserId(user.getId()).size());

        // Verify audit logs
        assertEquals(1, auditLogRepository.findByUsername(user.getUsername(),
                PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void testAllRepositoriesTogether() {
        LocalDateTime testTime = LocalDateTime.now();
        long timestamp = System.currentTimeMillis();

        // Create multiple users with unique usernames
        User admin = userRepository.save(User.builder()
                .username("admin_user_" + timestamp)
                .password("admin_pass")
                .email("admin_" + timestamp + "@test.com")
                .role(User.Role.ROLE_ADMIN)
                .build());

        User regularUser = userRepository.save(User.builder()
                .username("regular_user_" + timestamp)
                .password("user_pass")
                .email("user_" + timestamp + "@test.com")
                .role(User.Role.ROLE_USER)
                .build());

        // Create cards
        Card adminCard = cardRepository.save(Card.builder()
                .cardNumber("1111111111111111")
                .cardHolder("ADMIN USER")
                .balance(new BigDecimal("5000.00"))
                .expiryDate(testTime.plusYears(2).toLocalDate())
                .status(Card.CardStatus.ACTIVE)
                .user(admin)
                .build());

        Card userCard = cardRepository.save(Card.builder()
                .cardNumber("2222222222222222")
                .cardHolder("REGULAR USER")
                .balance(new BigDecimal("1000.00"))
                .expiryDate(testTime.plusYears(1).toLocalDate())
                .status(Card.CardStatus.ACTIVE)
                .user(regularUser)
                .build());

        // Create transaction between users
        Transaction transaction = transactionRepository.save(Transaction.builder()
                .fromCard(adminCard)
                .toCard(userCard)
                .amount(new BigDecimal("500.00"))
                .transactionDate(testTime)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build());

        // Create audit logs for actions
        auditLogRepository.save(AuditLog.builder()
                .username(admin.getUsername())
                .actionType("MONEY_TRANSFER")
                .description("Transferred $500 to " + regularUser.getUsername())
                .success(true)
                .timestamp(testTime)
                .ipAddress("192.168.1.10")
                .resourceId("transfer-" + transaction.getId())
                .build());

        auditLogRepository.save(AuditLog.builder()
                .username(regularUser.getUsername())
                .actionType("RECEIVE_MONEY")
                .description("Received $500 from " + admin.getUsername())
                .success(true)
                .timestamp(testTime)
                .ipAddress("192.168.1.20")
                .resourceId("transfer-" + transaction.getId())
                .build());

        // Verify counts
        assertEquals(2, userRepository.count());
        assertEquals(2, cardRepository.count());
        assertEquals(1, transactionRepository.count());
        assertEquals(2, auditLogRepository.count());
    }
}
