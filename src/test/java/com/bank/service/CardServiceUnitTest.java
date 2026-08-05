package com.bank.service;

import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.exception.*;
import com.bank.repository.CardRepository;
import com.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceUnitTest {

    @Mock private CardRepository cardRepository;
    @Mock private EncryptionService encryptionService;
    @Mock private TransactionRepository transactionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AuditService auditService;

    @InjectMocks private CardService cardService;

    private User testUser;
    private User adminUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .role(User.Role.ROLE_USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .role(User.Role.ROLE_ADMIN)
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted123")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(testUser)
                .build();
    }

    @Test
    void getUserCards_WithSearch_ShouldReturnFilteredCards() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Card> expectedPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findByUserAndSearch(eq(testUser), eq("test"), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Card> result = cardService.getUserCards(testUser, pageable, "test");

        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findByUserAndSearch(testUser, "test", pageable);
    }

    @Test
    void getUserCards_WithoutSearch_ShouldReturnAllCards() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Card> expectedPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findByUser(eq(testUser), eq(pageable)))
                .thenReturn(expectedPage);

        Page<Card> result = cardService.getUserCards(testUser, pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findByUser(testUser, pageable);
    }

    @Test
    void getCardById_AdminAccess_ShouldReturnCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        Card result = cardService.getCardById(1L, adminUser);

        assertEquals(testCard.getId(), result.getId());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_CardNotFound_ShouldThrowException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(1L, testUser));
    }

    @Test
    void transferBetweenCards_InsufficientBalance_ShouldThrowException() {
        Card fromCard = Card.builder()
                .id(1L)
                .balance(new BigDecimal("50.00"))
                .user(testUser)
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        Card toCard = Card.builder()
                .id(2L)
                .balance(new BigDecimal("100.00"))
                .user(testUser)
                .status(Card.CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientBalanceException.class,
                () -> cardService.transferBetweenCards(testUser, 1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transferBetweenCards_SameCard_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> cardService.transferBetweenCards(testUser, 1L, 1L, new BigDecimal("100.00")));
    }

    @Test
    void createCard_AlreadyExists_ShouldThrowException() {
        when(cardRepository.existsByCardNumber("encrypted123")).thenReturn(true);

        assertThrows(CardAlreadyExistsException.class,
                () -> cardService.createCard(testCard));
    }

    @Test
    void createCard_ExpiredCard_ShouldSetExpiredStatus() {
        Card expiredCard = Card.builder()
                .cardNumber("newcard")
                .expiryDate(LocalDate.now().minusDays(1))
                .user(testUser)
                .build();

        when(cardRepository.existsByCardNumber("newcard")).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(expiredCard);

        cardService.createCard(expiredCard);

        verify(cardRepository).save(argThat(card ->
                card.getStatus() == Card.CardStatus.EXPIRED));
    }

    @Test
    void requestCardBlock_UnauthorizedUser_ShouldThrowException() {
        User otherUser = User.builder()
                .id(3L)
                .username("other")
                .role(User.Role.ROLE_USER)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        assertThrows(UnauthorizedAccessException.class,
                () -> cardService.requestCardBlock(1L, otherUser));
    }

    @Test
    void getMaskedCardNumber_NullInput_ShouldReturnDefault() {
        String result = cardService.getMaskedCardNumber(null);

        assertEquals("**** **** **** ****", result);
    }

    @Test
    void getMaskedCardNumber_DecryptionError_ShouldReturnDefault() {
        when(encryptionService.decrypt("invalid")).thenThrow(new RuntimeException("Error"));

        String result = cardService.getMaskedCardNumber("invalid");

        assertEquals("**** **** **** ****", result);
    }

    @Test
    void createCard_WithNullCard_ShouldThrowNullPointerException() {
        // CardService выбрасывает NullPointerException с сообщением "Card cannot be null"
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> cardService.createCard(null));

        assertEquals("Card cannot be null", exception.getMessage());
    }

    @Test
    void transferBetweenCards_WithNullAmount_ShouldThrowIllegalArgumentException() {
        // Проверьте реальное поведение в CardService
        // Если метод выбрасывает IllegalArgumentException для null amount
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cardService.transferBetweenCards(testUser, 1L, 2L, null));

        // Или NPE, в зависимости от реализации
    }

    @Test
    void createCard_WithNullBalance_ShouldSetZeroBalance() {
        Card cardWithoutBalance = Card.builder()
                .cardNumber("testcard")
                .expiryDate(LocalDate.now().plusYears(1))
                .user(testUser)
                .build();

        when(cardRepository.existsByCardNumber("testcard")).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(cardWithoutBalance);

        cardService.createCard(cardWithoutBalance);

        verify(cardRepository).save(argThat(card ->
                card.getBalance() != null && card.getBalance().equals(BigDecimal.ZERO)));
    }

    @Test
    void createCard_WithValidCard_ShouldSetActiveStatus() {
        Card validCard = Card.builder()
                .cardNumber("validcard")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("500.00"))
                .user(testUser)
                .build();

        when(cardRepository.existsByCardNumber("validcard")).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(validCard);

        cardService.createCard(validCard);

        verify(cardRepository).save(argThat(card ->
                card.getStatus() == Card.CardStatus.ACTIVE));
    }

    @Test
    void updateCardStatus_ShouldUpdateStatusAndResetBlockRequest() {
        Card card = Card.builder()
                .id(1L)
                .status(Card.CardStatus.ACTIVE)
                .blockRequested(true)
                .user(testUser)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.updateCardStatus(1L, Card.CardStatus.BLOCKED);

        assertEquals(Card.CardStatus.BLOCKED, result.getStatus());
        assertFalse(result.getBlockRequested());
        verify(cardRepository).save(argThat(c ->
                c.getStatus() == Card.CardStatus.BLOCKED && !c.getBlockRequested()
        ));
    }

    @Test
    void updateCardStatus_CardNotFound_ShouldThrowException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.updateCardStatus(1L, Card.CardStatus.BLOCKED));
    }
}
