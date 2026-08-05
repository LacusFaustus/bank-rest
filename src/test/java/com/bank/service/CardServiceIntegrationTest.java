package com.bank.service;

import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.exception.CardNotFoundException;
import com.bank.exception.UnauthorizedAccessException;
import com.bank.repository.CardRepository;
import com.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardServiceIntegrationTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User adminUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        // Очищаем базу
        cardRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        testUser = User.builder()
                .username("testuser_" + System.currentTimeMillis())
                .password(passwordEncoder.encode("password"))
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
        userRepository.save(testUser);

        // Создаем администратора
        adminUser = User.builder()
                .username("admin_" + System.currentTimeMillis())
                .password(passwordEncoder.encode("adminpass"))
                .email("admin@bank.com")
                .role(User.Role.ROLE_ADMIN)
                .build();
        userRepository.save(adminUser);

        // Создаем тестовую карту
        testCard = Card.builder()
                .cardNumber("encrypted1234567890123456")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(testUser)
                .build();
        cardRepository.save(testCard);
    }

    @Test
    void getCardById_UserOwnsCard_ShouldReturnCard() {
        // When
        Card result = cardService.getCardById(testCard.getId(), testUser);

        // Then
        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
        assertEquals(testCard.getBalance(), result.getBalance());
    }

    @Test
    void getCardById_AdminAccess_ShouldReturnCard() {
        // When
        Card result = cardService.getCardById(testCard.getId(), adminUser);

        // Then
        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
    }

    @Test
    void getCardById_CardNotFound_ShouldThrowException() {
        // When & Then
        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(999L, testUser));
    }

    @Test
    void getCardById_UnauthorizedUser_ShouldThrowException() {
        // Given
        User otherUser = User.builder()
                .username("otheruser_" + System.currentTimeMillis())
                .password(passwordEncoder.encode("pass"))
                .email("other@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
        userRepository.save(otherUser);

        // When & Then
        assertThrows(UnauthorizedAccessException.class,
                () -> cardService.getCardById(testCard.getId(), otherUser));
    }
}
