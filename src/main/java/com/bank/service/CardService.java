package com.bank.service;

import com.bank.entity.Card;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.event.CardBlockRequestedEvent;
import com.bank.event.TransferCompletedEvent;
import com.bank.exception.*;
import com.bank.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;
    private final TransactionService transactionService;
    private final MonitoringService monitoringService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    public Page<Card> getUserCards(User user, Pageable pageable, String search) {
        log.debug("Getting cards for user: id={}, username={}", user.getId(), user.getUsername());

        if (search != null && !search.trim().isEmpty()) {
            return cardRepository.findByUserAndCardHolderContainingIgnoreCase(user, search.trim(), pageable);
        }

        return cardRepository.findByUser(user, pageable);
    }

    public List<Card> getUserActiveCards(User user) {
        return cardRepository.findActiveUserCards(user);
    }

    public Card getCardById(Long cardId, User user) {
        log.debug("Getting card {} for user: id={}, username={}", cardId, user.getId(), user.getUsername());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        // Проверяем, что пользователь является владельцем карты или администратором
        boolean isOwner = card.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(User.Role.ROLE_ADMIN);

        log.debug("Card user id: {}, current user id: {}, isOwner: {}, isAdmin: {}",
                card.getUser().getId(), user.getId(), isOwner, isAdmin);

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedAccessException("Access denied to card");
        }

        return card;
    }

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
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
        // Проверка на дубликат карты
        if (cardRepository.existsByCardNumber(card.getCardNumber())) {
            throw new CardAlreadyExistsException("Card with this number already exists");
        }

        // Устанавливаем начальный баланс
        if (card.getBalance() == null) {
            card.setBalance(BigDecimal.ZERO);
        }

        // Проверяем срок действия
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(Card.CardStatus.EXPIRED);
        } else {
            card.setStatus(Card.CardStatus.ACTIVE);
        }

        Card savedCard = cardRepository.save(card);
        log.info("Card created successfully: {} for user {}", savedCard.getId(),
                card.getUser().getUsername());

        // Логируем создание карты
        auditService.logActivity("CARD_CREATE",
                String.format("Card created: %s for user %s", savedCard.getId(), card.getUser().getUsername()),
                true, savedCard.getId().toString(), null, null);

        return savedCard;
    }

    @Transactional
    public Card updateCardStatus(Long cardId, Card.CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        card.setStatus(status);
        Card updatedCard = cardRepository.save(card);

        // Логируем изменение статуса
        auditService.logActivity("CARD_STATUS_UPDATE",
                String.format("Card status updated to %s", status),
                true, cardId.toString(), null, null);

        log.info("Card status updated: {} -> {}", cardId, status);
        return updatedCard;
    }

    @Transactional
    public void requestCardBlock(Long cardId, User user) {
        Card card = getCardById(cardId, user);
        card.setBlockRequested(true);
        cardRepository.save(card);

        // Публикуем событие запроса блокировки
        eventPublisher.publishEvent(new CardBlockRequestedEvent(this, card, "User requested block"));

        monitoringService.recordCardBlockRequest(cardId, user.getUsername());

        // Логируем запрос блокировки
        auditService.logActivity("CARD_BLOCK_REQUEST",
                String.format("Block requested for card %s", cardId),
                true, cardId.toString(), null, null);

        log.info("Block requested for card: {} by user: {}", cardId, user.getUsername());
    }

    public List<Card> getCardsWithBlockRequests() {
        return cardRepository.findByBlockRequestedTrue();
    }

    @Transactional
    public void transferBetweenCards(User user, Long fromCardId, Long toCardId, BigDecimal amount) {
        long startTime = System.currentTimeMillis();

        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Transfer amount must be positive");
            }

            if (fromCardId.equals(toCardId)) {
                throw new IllegalArgumentException("Cannot transfer to the same card");
            }

            Card fromCard = getCardById(fromCardId, user);
            Card toCard = getCardById(toCardId, user);

            // Проверяем, что обе карты принадлежат пользователю
            if (!fromCard.getUser().getId().equals(user.getId()) ||
                    !toCard.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("You can only transfer between your own cards");
            }

            if (!fromCard.isActive()) {
                throw new IllegalStateException("Source card is not active");
            }

            if (!toCard.isActive()) {
                throw new IllegalStateException("Destination card is not active");
            }

            if (fromCard.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance for transfer");
            }

            // Выполняем перевод
            fromCard.setBalance(fromCard.getBalance().subtract(amount));
            toCard.setBalance(toCard.getBalance().add(amount));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            // Создаем запись о транзакции
            Transaction transaction = Transaction.builder()
                    .fromCard(fromCard)
                    .toCard(toCard)
                    .amount(amount)
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .build();

            Transaction savedTransaction = transactionService.saveTransaction(transaction);

            // Публикуем событие успешного перевода
            eventPublisher.publishEvent(new TransferCompletedEvent(this, savedTransaction, fromCard, toCard, amount));

            long duration = System.currentTimeMillis() - startTime;
            monitoringService.recordTransfer(user.getUsername(), toCard.getUser().getUsername(), amount);

            // Логируем успешный перевод
            auditService.logActivity("TRANSFER_SUCCESS",
                    String.format("Transfer %.2f from card %s to card %s", amount, fromCardId, toCardId),
                    true, fromCardId.toString(),
                    String.format("{\"fromCardId\": %d, \"toCardId\": %d, \"amount\": %.2f}", fromCardId, toCardId, amount),
                    null);

            log.info("Transfer completed: {} from card {} to card {} ({} ms)",
                    amount, fromCardId, toCardId, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            // Логируем неудачный перевод
            auditService.logActivity("TRANSFER_FAILED",
                    String.format("Transfer failed: %.2f from card %s to card %s - %s", amount, fromCardId, toCardId, e.getMessage()),
                    false, fromCardId.toString(),
                    String.format("{\"fromCardId\": %d, \"toCardId\": %d, \"amount\": %.2f}", fromCardId, toCardId, amount),
                    null);

            log.error("Transfer failed: {} from card {} to card {} ({} ms) - Error: {}",
                    amount, fromCardId, toCardId, duration, e.getMessage());
            throw e;
        }
    }

    public String getMaskedCardNumber(String encryptedCardNumber) {
        try {
            String decrypted = encryptionService.decrypt(encryptedCardNumber);
            if (decrypted != null && decrypted.length() >= 4) {
                return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
            }
            return "**** **** **** ****";
        } catch (Exception e) {
            log.warn("Error decrypting card number for masking: {}", e.getMessage());
            return "**** **** **** ****";
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndUpdateExpiredCards() {
        List<Card> expiredCards = cardRepository.findByStatusAndExpiryDateBefore(
                Card.CardStatus.ACTIVE, LocalDate.now());

        for (Card card : expiredCards) {
            card.setStatus(Card.CardStatus.EXPIRED);
            log.info("Card {} expired and was deactivated", card.getId());

            // Логируем автоматическое истечение срока
            auditService.logActivity("CARD_EXPIRED_AUTO",
                    String.format("Card %s expired automatically", card.getId()),
                    true, card.getId().toString(), null, null);
        }

        if (!expiredCards.isEmpty()) {
            cardRepository.saveAll(expiredCards);
        }
    }

    public boolean isCardOwnedByUser(Long cardId, User user) {
        return cardRepository.findByIdAndUser(cardId, user).isPresent();
    }
}
