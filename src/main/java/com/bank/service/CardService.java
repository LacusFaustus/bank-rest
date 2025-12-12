package com.bank.service;

import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.entity.Transaction;
import com.bank.event.CardBlockRequestedEvent;
import com.bank.event.TransferCompletedEvent;
import com.bank.exception.*;
import com.bank.repository.CardRepository;
import com.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    // ========== PUBLIC METHODS FOR CONTROLLERS ==========

    @Transactional(readOnly = true)
    public Page<Card> getUserCards(User user, Pageable pageable, String search) {
        log.debug("Getting cards for user: {}", user.getUsername());

        if (search != null && !search.trim().isEmpty()) {
            return cardRepository.findByUserAndSearch(user, search.trim(), pageable);
        }
        return cardRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Card> getActiveUserCards(User user, Pageable pageable) {
        log.debug("Getting active cards for user: {}", user.getUsername());

        Page<Card> allCards = getUserCards(user, pageable, null);

        // Filter active cards
        List<Card> activeCards = allCards.getContent().stream()
                .filter(Card::isActive)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(activeCards, pageable, activeCards.size());
    }

    @Transactional(readOnly = true)
    public Card getCardById(Long cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        if (!hasAccessToCard(card, user)) {
            log.warn("Access denied for user {} to card {}", user.getUsername(), cardId);
            throw new UnauthorizedAccessException("Access denied to card");
        }

        return card;
    }

    @Transactional(readOnly = true)
    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Card> getCardsWithBlockRequests(Pageable pageable) {
        Page<Card> allCards = cardRepository.findAll(pageable);

        // Filter cards with block requests
        List<Card> blockedCards = allCards.getContent().stream()
                .filter(card -> Boolean.TRUE.equals(card.getBlockRequested()))
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(blockedCards, pageable, blockedCards.size());
    }

    @Transactional(readOnly = true)
    public Page<Card> getExpiredCards(Pageable pageable) {
        Page<Card> allCards = cardRepository.findAll(pageable);

        // Filter expired cards
        List<Card> expiredCards = allCards.getContent().stream()
                .filter(card -> card.getStatus() == Card.CardStatus.EXPIRED)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(expiredCards, pageable, expiredCards.size());
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Card not found with id: " + cardId);
        }
        cardRepository.deleteById(cardId);
        log.info("Card deleted successfully: {}", cardId);
    }

    @Transactional
    public Card createCard(Card card) {
        if (card == null) {
            throw new NullPointerException("Card cannot be null");
        }

        if (cardRepository.existsByCardNumber(card.getCardNumber())) {
            throw new CardAlreadyExistsException("Card with this number already exists");
        }

        if (cardRepository.existsByCardNumber(card.getCardNumber())) {
            throw new CardAlreadyExistsException("Card with this number already exists");
        }

        if (card.getBalance() == null) {
            card.setBalance(BigDecimal.ZERO);
        }

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(Card.CardStatus.EXPIRED);
        } else {
            card.setStatus(Card.CardStatus.ACTIVE);
        }

        Card savedCard = cardRepository.save(card);
        log.info("Card created: {} for user {}", savedCard.getId(), card.getUser().getUsername());

        return savedCard;
    }

    @Transactional
    public Card updateCardStatus(Long cardId, Card.CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        card.setStatus(status);

        // If card is blocked, reset block request flag
        if (status == Card.CardStatus.BLOCKED) {
            card.setBlockRequested(false);
        }

        Card updatedCard = cardRepository.save(card);

        log.info("Card status updated: {} -> {}", cardId, status);
        auditService.logActivity("CARD_STATUS_UPDATE",
                String.format("Card %s status updated to %s", cardId, status),
                true, cardId.toString(), null, null);

        return updatedCard;
    }

    @Transactional
    public void requestCardBlock(Long cardId, User user) {
        Card card = getCardById(cardId, user);

        if (user.getRole() != User.Role.ROLE_ADMIN && !card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only request block for your own cards");
        }

        card.setBlockRequested(true);
        cardRepository.save(card);

        eventPublisher.publishEvent(new CardBlockRequestedEvent(this, card, "User requested block"));

        log.info("Block requested for card: {} by user: {}", cardId, user.getUsername());
        auditService.logActivity("CARD_BLOCK_REQUEST",
                String.format("Block requested for card %s by user %s", cardId, user.getUsername()),
                true, cardId.toString(), null, null);
    }

    @Transactional
    public void transferBetweenCards(User user, Long fromCardId, Long toCardId, BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Transfer amount cannot be null");
        }
        validateTransferParameters(amount, fromCardId, toCardId);

        Card fromCard = getCardById(fromCardId, user);
        Card toCard = getCardById(toCardId, user);

        validateCardsForTransfer(user, fromCard, toCard);
        validateSufficientBalance(fromCard, amount);

        performTransfer(fromCard, toCard, amount, user);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private boolean hasAccessToCard(Card card, User user) {
        return card.getUser().getId().equals(user.getId())
                || user.getRole() == User.Role.ROLE_ADMIN;
    }

    private void validateTransferParameters(BigDecimal amount, Long fromCardId, Long toCardId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }
    }

    private void validateCardsForTransfer(User user, Card fromCard, Card toCard) {
        if (user.getRole() != User.Role.ROLE_ADMIN) {
            if (!fromCard.getUser().getId().equals(user.getId())
                    || !toCard.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("You can only transfer between your own cards");
            }
        }

        if (!fromCard.isActive()) {
            throw new IllegalStateException("Source card is not active");
        }
        if (!toCard.isActive()) {
            throw new IllegalStateException("Destination card is not active");
        }
    }

    private void validateSufficientBalance(Card fromCard, BigDecimal amount) {
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for transfer");
        }
    }

    private void performTransfer(Card fromCard, Card toCard, BigDecimal amount, User user) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(amount)
                .status(Transaction.TransactionStatus.SUCCESS)
                .transactionDate(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        // Publish event
        eventPublisher.publishEvent(new TransferCompletedEvent(
                this, transaction, fromCard, toCard, amount));

        log.info("Transfer completed: {} from card {} to card {} by user {}",
                amount, fromCard.getId(), toCard.getId(), user.getUsername());

        auditService.logActivity("TRANSFER_COMPLETED",
                String.format("Transfer of %s from card %s to card %s",
                        amount, fromCard.getId(), toCard.getId()),
                true, fromCard.getId().toString(), toCard.getId().toString(), null);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndUpdateExpiredCards() {
        List<Card> expiredCards = cardRepository.findByStatusAndExpiryDateBefore(
                Card.CardStatus.ACTIVE, LocalDate.now());

        for (Card card : expiredCards) {
            card.setStatus(Card.CardStatus.EXPIRED);
            log.info("Card {} expired and was deactivated", card.getId());
        }

        if (!expiredCards.isEmpty()) {
            cardRepository.saveAll(expiredCards);
        }
    }

    public String getMaskedCardNumber(String encryptedCardNumber) {
        try {
            if (encryptedCardNumber == null) return "**** **** **** ****";

            String decrypted = encryptionService.decrypt(encryptedCardNumber);
            if (decrypted != null && decrypted.length() >= 4) {
                return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
            }
            return "**** **** **** ****";
        } catch (Exception e) {
            log.warn("Error decrypting card number: {}", e.getMessage());
            return "**** **** **** ****";
        }
    }
}
