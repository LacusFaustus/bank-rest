package com.bank.controller;

import com.bank.dto.CardResponseDTO;
import com.bank.dto.TransferRequest;
import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.service.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
    }

    private Card createTestCard(Long id, String cardNumber) {
        return Card.builder()
                .id(id)
                .cardNumber(cardNumber)
                .cardHolder("TEST USER")
                .balance(BigDecimal.valueOf(1000.00))
                .status(Card.CardStatus.ACTIVE)
                .build();
    }

    @Test
    void getUserCards_ShouldReturnCards() {
        // Given
        User user = createTestUser();
        Card card = createTestCard(1L, "1234567890123456");
        Page<Card> cardPage = new PageImpl<>(List.of(card));

        when(cardService.getUserCards(eq(user), any(PageRequest.class), eq(null)))
                .thenReturn(cardPage);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<Page<CardResponseDTO>> response = cardController.getUserCards(user, 0, 10, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        CardResponseDTO dto = response.getBody().getContent().get(0);
        assertEquals("123456******3456", dto.getMaskedCardNumber());
    }

    @Test
    void getCard_ShouldReturnCardById() {
        // Given
        User user = createTestUser();
        Card card = createTestCard(1L, "1234567890123456");

        when(cardService.getCardById(1L, user)).thenReturn(card);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<CardResponseDTO> response = cardController.getCard(user, 1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("123456******3456", response.getBody().getMaskedCardNumber());
    }

    @Test
    void requestBlockCard_ShouldSubmitRequest() {
        // Given
        User user = createTestUser();

        // When
        ResponseEntity<?> response = cardController.requestBlockCard(user, 1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Block request submitted for card: 1", response.getBody());
        verify(cardService).requestCardBlock(1L, user);
    }

    @Test
    void transferBetweenCards_ShouldCompleteTransfer() {
        // Given
        User user = createTestUser();
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));

        // When
        ResponseEntity<?> response = cardController.transferBetweenCards(user, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Transfer completed successfully", response.getBody());
        verify(cardService).transferBetweenCards(user, 1L, 2L, BigDecimal.valueOf(100.00));
    }

    @Test
    void getCardBalance_ShouldReturnBalance() {
        // Given
        User user = createTestUser();
        Card card = createTestCard(1L, "1234567890123456");

        when(cardService.getCardById(1L, user)).thenReturn(card);

        // When
        ResponseEntity<BigDecimal> response = cardController.getCardBalance(user, 1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(1000.00), response.getBody());
    }

    @Test
    void getActiveCards_ShouldReturnActiveCards() {
        // Given
        User user = createTestUser();
        Card activeCard = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .status(Card.CardStatus.ACTIVE)
                .build();

        Page<Card> cardPage = new PageImpl<>(List.of(activeCard));

        when(cardService.getActiveUserCards(eq(user), any(PageRequest.class)))
                .thenReturn(cardPage);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<Page<CardResponseDTO>> response = cardController.getActiveCards(user, 0, 10);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getUserCards_WithSearchParam_ShouldFilterCards() {
        // Given
        User user = createTestUser();
        Card card = createTestCard(1L, "1234567890123456");
        Page<Card> cardPage = new PageImpl<>(List.of(card));

        when(cardService.getUserCards(eq(user), any(PageRequest.class), eq("1234")))
                .thenReturn(cardPage);
        when(cardService.getMaskedCardNumber(anyString())).thenReturn("123456******3456");

        // When
        ResponseEntity<Page<CardResponseDTO>> response = cardController.getUserCards(user, 0, 10, "1234");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cardService).getUserCards(user, PageRequest.of(0, 10), "1234");
    }

    @Test
    void getCard_UnauthorizedUser_ShouldThrowException() {
        // Given
        User user = createTestUser();

        when(cardService.getCardById(1L, user))
                .thenThrow(new RuntimeException("Card not found or access denied"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            cardController.getCard(user, 1L);
        });
    }

    @Test
    void transferBetweenCards_InvalidAmount_ShouldThrowException() {
        // Given
        User user = createTestUser();
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(-100.00));

        doThrow(new IllegalArgumentException("Invalid amount"))
                .when(cardService).transferBetweenCards(any(), anyLong(), anyLong(), any());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            cardController.transferBetweenCards(user, request);
        });
    }

    @Test
    void getUserCards_EmptyResult_ShouldReturnEmptyPage() {
        // Given
        User user = createTestUser();
        Page<Card> emptyPage = Page.empty();

        when(cardService.getUserCards(eq(user), any(PageRequest.class), eq(null)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<Page<CardResponseDTO>> response = cardController.getUserCards(user, 0, 10, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
    }
}
