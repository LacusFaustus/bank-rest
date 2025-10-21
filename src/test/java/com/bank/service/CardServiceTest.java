package com.bank.service;

import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.exception.CardNotFoundException;
import com.bank.exception.UnauthorizedAccessException;
import com.bank.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuditService auditService;

    private CardService cardService;

    private User testUser;
    private User adminUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, encryptionService, transactionService,
                monitoringService, eventPublisher, auditService);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .password("adminpass")
                .email("admin@bank.com")
                .role(User.Role.ROLE_ADMIN)
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted1234567890123456")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(testUser)
                .build();
    }

    @Test
    void getCardById_UserOwnsCard_ShouldReturnCard() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When
        Card result = cardService.getCardById(1L, testUser);

        // Then
        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
    }

    @Test
    void getCardById_AdminUser_ShouldReturnCard() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When
        Card result = cardService.getCardById(1L, adminUser);

        // Then
        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
    }

    @Test
    void getCardById_CardNotFound_ShouldThrowException() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(1L, testUser));
    }

    @Test
    void getCardById_UserNotOwnerAndNotAdmin_ShouldThrowException() {
        // Given
        User otherUser = User.builder()
                .id(3L)
                .username("otheruser")
                .role(User.Role.ROLE_USER)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(UnauthorizedAccessException.class,
                () -> cardService.getCardById(1L, otherUser));
    }

    @Test
    void getMaskedCardNumber_ShouldReturnMaskedNumber() {
        // Given
        when(encryptionService.decrypt("encrypted1234567890123456")).thenReturn("1234567890123456");

        // When
        String masked = cardService.getMaskedCardNumber("encrypted1234567890123456");

        // Then
        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void getMaskedCardNumber_DecryptionFails_ShouldReturnDefault() {
        // Given
        when(encryptionService.decrypt("invalid-encrypted")).thenThrow(new RuntimeException("Decryption error"));

        // When
        String masked = cardService.getMaskedCardNumber("invalid-encrypted");

        // Then
        assertEquals("**** **** **** ****", masked);
    }
}
