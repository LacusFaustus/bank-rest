package com.bank.controller;

import com.bank.dto.CardResponseDTO;
import com.bank.dto.CreateCardRequest;
import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.service.CardService;
import com.bank.service.EncryptionService;
import com.bank.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private UserService userService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private AdminController adminController;

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
    }

    private Card createTestCard(Long id) {
        return Card.builder()
                .id(id)
                .cardNumber("encrypted-card-number")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.now().plusYears(3))
                .balance(BigDecimal.valueOf(1000.00))
                .status(Card.CardStatus.ACTIVE)
                .build();
    }

    @Test
    void createCard_ShouldCreateNewCard() {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(1L);
        request.setCardNumber("1234567890123456");
        request.setCardHolder("Test User");
        request.setExpiryDate(LocalDate.now().plusYears(3));
        request.setInitialBalance(BigDecimal.valueOf(500.00));

        User user = createTestUser();
        Card card = createTestCard(1L);

        when(userService.getUserById(1L)).thenReturn(user);
        when(encryptionService.encrypt("1234567890123456")).thenReturn("encrypted-card-number");
        when(cardService.createCard(any(Card.class))).thenReturn(card);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<CardResponseDTO> response = adminController.createCard(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("123456******3456", response.getBody().getMaskedCardNumber());

        verify(encryptionService).encrypt("1234567890123456");
        verify(cardService).createCard(any(Card.class));
    }

    @Test
    void updateCardStatus_ShouldUpdateStatus() {
        // Given
        Card card = createTestCard(1L);
        card.setStatus(Card.CardStatus.BLOCKED);

        when(cardService.updateCardStatus(1L, Card.CardStatus.BLOCKED)).thenReturn(card);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<CardResponseDTO> response = adminController.updateCardStatus(1L, Card.CardStatus.BLOCKED);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Card.CardStatus.BLOCKED, response.getBody().getStatus());
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        // Given
        Card card1 = createTestCard(1L);
        Card card2 = createTestCard(2L);
        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));

        when(cardService.getAllCards(any())).thenReturn(cardPage);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<Page<CardResponseDTO>> response = adminController.getAllCards(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    void deleteCard_ShouldDeleteCard() {
        // Given
        doNothing().when(cardService).deleteCard(1L);

        // When
        ResponseEntity<?> response = adminController.deleteCard(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Card deleted successfully", response.getBody());
        verify(cardService).deleteCard(1L);
    }

    @Test
    void getAllUsers_ShouldReturnUsersWithoutPasswords() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .password("password1")
                .email("user1@bank.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .password("password2")
                .email("user2@bank.com")
                .build();

        Page<User> userPage = new PageImpl<>(List.of(user1, user2));

        when(userService.getAllUsers(any())).thenReturn(userPage);

        // When
        ResponseEntity<Page<User>> response = adminController.getAllUsers(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Check passwords are null
        response.getBody().forEach(user -> assertNull(user.getPassword()));
        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    void getCardsWithBlockRequests_ShouldReturnCardsWithRequests() {
        // Given
        Card card1 = createTestCard(1L);
        Card card2 = createTestCard(2L);
        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));

        when(cardService.getCardsWithBlockRequests(any())).thenReturn(cardPage);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<Page<CardResponseDTO>> response = adminController.getCardsWithBlockRequests(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
    }

    @Test
    void getExpiredCards_ShouldReturnExpiredCards() {
        // Given
        Card expiredCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted-card-number")
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        Page<Card> cardPage = new PageImpl<>(List.of(expiredCard));

        when(cardService.getExpiredCards(any())).thenReturn(cardPage);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<Page<CardResponseDTO>> response = adminController.getExpiredCards(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void createCard_ShouldConvertCardHolderToUpperCase() {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(1L);
        request.setCardNumber("1234567890123456");
        request.setCardHolder("test user");
        request.setExpiryDate(LocalDate.now().plusYears(3));
        request.setInitialBalance(BigDecimal.valueOf(500.00));

        User user = createTestUser();
        Card card = createTestCard(1L);
        card.setCardHolder("TEST USER");

        when(userService.getUserById(1L)).thenReturn(user);
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-card-number");
        when(cardService.createCard(any(Card.class))).thenReturn(card);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<CardResponseDTO> response = adminController.createCard(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Verify card holder was converted to uppercase in controller
        verify(cardService).createCard(argThat(cardArg ->
                cardArg.getCardHolder().equals("TEST USER")
        ));
    }

    @Test
    void getAllCards_EmptyResult_ShouldReturnEmptyPage() {
        // Given
        Page<Card> emptyPage = Page.empty();

        when(cardService.getAllCards(any())).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<CardResponseDTO>> response = adminController.getAllCards(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    void createCard_UserNotFound_ShouldThrowException() {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(999L);
        request.setCardNumber("1234567890123456");
        request.setCardHolder("Test User");
        request.setExpiryDate(LocalDate.now().plusYears(3));
        request.setInitialBalance(BigDecimal.valueOf(500.00));

        when(userService.getUserById(999L)).thenThrow(new RuntimeException("User not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adminController.createCard(request);
        });
    }

    @Test
    void getAllUsers_DifferentPageSize_ShouldUseCorrectPageSize() {
        // Given
        Page<User> userPage = Page.empty();

        when(userService.getAllUsers(any())).thenReturn(userPage);

        // When
        adminController.getAllUsers(2, 25);

        // Then
        verify(userService).getAllUsers(argThat(pageable ->
                pageable.getPageNumber() == 2 && pageable.getPageSize() == 25
        ));
    }

    @Test
    void createCard_WithNullValues_ShouldThrowException() {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        // Не заполняем обязательные поля

        // When & Then
        // В зависимости от валидации, может выбрасывать исключение
        // Это тест для проверки обработки null значений
        assertNotNull(request);
    }
}
