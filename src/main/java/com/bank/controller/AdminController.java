package com.bank.controller;

import com.bank.dto.CardResponseDTO;
import com.bank.dto.CreateCardRequest;
import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.service.CardService;
import com.bank.service.EncryptionService;
import com.bank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final CardService cardService;
    private final UserService userService;
    private final EncryptionService encryptionService;

    /**
     * Создать новую банковскую карту
     */
    @PostMapping("/cards")
    public ResponseEntity<CardResponseDTO> createCard(@Valid @RequestBody CreateCardRequest request) {
        User user = userService.getUserById(request.getUserId());

        // Шифруем номер карты
        String encryptedCardNumber = encryptionService.encrypt(request.getCardNumber());

        Card card = Card.builder()
                .cardNumber(encryptedCardNumber)
                .cardHolder(request.getCardHolder().toUpperCase())
                .expiryDate(request.getExpiryDate())
                .balance(request.getInitialBalance())
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();

        Card savedCard = cardService.createCard(card);

        CardResponseDTO response = CardResponseDTO.fromEntity(
                savedCard,
                cardService.getMaskedCardNumber(savedCard.getCardNumber())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Обновить статус карты
     */
    @PutMapping("/cards/{cardId}/status")
    public ResponseEntity<CardResponseDTO> updateCardStatus(
            @PathVariable Long cardId,
            @RequestParam Card.CardStatus status) {

        Card card = cardService.updateCardStatus(cardId, status);

        CardResponseDTO response = CardResponseDTO.fromEntity(
                card,
                cardService.getMaskedCardNumber(card.getCardNumber())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Получить все карты всех пользователей
     */
    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponseDTO>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Card> cards = cardService.getAllCards(PageRequest.of(page, size));

        Page<CardResponseDTO> response = cards.map(card ->
                CardResponseDTO.fromEntity(
                        card,
                        cardService.getMaskedCardNumber(card.getCardNumber())
                )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Удалить карту
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().body("Card deleted successfully");
    }

    /**
     * Получить всех пользователей
     */
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<User> users = userService.getAllUsers(PageRequest.of(page, size));
        // Не возвращаем пароли
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /**
     * Получить карты с запросами на блокировку
     */
    @GetMapping("/cards/block-requests")
    public ResponseEntity<Page<CardResponseDTO>> getCardsWithBlockRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Card> cards = cardService.getCardsWithBlockRequests(PageRequest.of(page, size));

        Page<CardResponseDTO> response = cards.map(card ->
                CardResponseDTO.fromEntity(
                        card,
                        cardService.getMaskedCardNumber(card.getCardNumber())
                )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Получить просроченные карты
     */
    @GetMapping("/cards/expired")
    public ResponseEntity<Page<CardResponseDTO>> getExpiredCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Card> cards = cardService.getExpiredCards(PageRequest.of(page, size));

        Page<CardResponseDTO> response = cards.map(card ->
                CardResponseDTO.fromEntity(
                        card,
                        cardService.getMaskedCardNumber(card.getCardNumber())
                )
        );

        return ResponseEntity.ok(response);
    }
}
