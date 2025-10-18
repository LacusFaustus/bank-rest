package com.bank.controller;

import com.bank.dto.CardResponseDTO;
import com.bank.dto.TransferRequest;
import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardResponseDTO>> getUserCards(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Page<Card> cards = cardService.getUserCards(user, PageRequest.of(page, size), search);

        Page<CardResponseDTO> response = cards.map(card ->
                CardResponseDTO.fromEntity(
                        card,
                        cardService.getMaskedCardNumber(card.getCardNumber())
                )
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponseDTO> getCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId) {

        Card card = cardService.getCardById(cardId, user);
        CardResponseDTO response = CardResponseDTO.fromEntity(
                card,
                cardService.getMaskedCardNumber(card.getCardNumber())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cardId}/block-request")
    public ResponseEntity<?> requestBlockCard(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId) {

        cardService.requestCardBlock(cardId, user);

        return ResponseEntity.ok().body("Block request submitted for card: " + cardId + ". Please contact administrator.");
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferBetweenCards(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request) {

        cardService.transferBetweenCards(user, request.getFromCardId(),
                request.getToCardId(), request.getAmount());

        return ResponseEntity.ok().body("Transfer completed successfully");
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getCardBalance(
            @AuthenticationPrincipal User user,
            @PathVariable Long cardId) {

        Card card = cardService.getCardById(cardId, user);
        return ResponseEntity.ok(card.getBalance());
    }
}
