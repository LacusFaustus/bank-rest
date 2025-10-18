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
        cardService = new CardService(
                cardRepository,
                encryptionService,
                transactionService,
                monitoringService,
                eventPublisher,
                auditService
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(User.Role.ROLE_USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(User.Role.ROLE_ADMIN);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("encrypted1234567890123456");
        testCard.setCardHolder("TEST USER");
        testCard.setExpiryDate(LocalDate.now().plusYears(2));
        testCard.setBalance(new BigDecimal("1000.00"));
        testCard.setStatus(Card.CardStatus.ACTIVE);
        testCard.setUser(testUser);
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
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_AdminAccess_ShouldReturnCard() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When
        Card result = cardService.getCardById(1L, adminUser);

        // Then
        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
    }

    @Test
    void getCardById_UserNotOwner_ShouldThrowException() {
        // Given
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUsername("other");
        otherUser.setRole(User.Role.ROLE_USER);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(UnauthorizedAccessException.class,
                () -> cardService.getCardById(1L, otherUser));
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
        when(encryptionService.decrypt("invalid")).thenThrow(new RuntimeException("Decryption failed"));

        // When
        String masked = cardService.getMaskedCardNumber("invalid");

        // Then
        assertEquals("**** **** **** ****", masked);
    }

    @Test
    void requestCardBlock_ShouldSetBlockRequestedFlag() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        cardService.requestCardBlock(1L, testUser);

        // Then
        assertTrue(testCard.getBlockRequested());
        verify(cardRepository).save(testCard);
        verify(monitoringService).recordCardBlockRequest(1L, "testuser");
    }

    @Test
    void isCardOwnedByUser_CardExistsAndOwned_ShouldReturnTrue() {
        // Given
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testCard));

        // When
        boolean result = cardService.isCardOwnedByUser(1L, testUser);

        // Then
        assertTrue(result);
    }

    @Test
    void isCardOwnedByUser_CardNotOwned_ShouldReturnFalse() {
        // Given
        when(cardRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // When
        boolean result = cardService.isCardOwnedByUser(1L, testUser);

        // Then
        assertFalse(result);
    }
}
