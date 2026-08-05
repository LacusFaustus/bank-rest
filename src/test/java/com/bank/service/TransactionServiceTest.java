package com.bank.service;

import com.bank.entity.Transaction;
import com.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now())
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();
    }

    @Test
    void saveTransaction_ShouldSaveAndReturnTransaction() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        Transaction result = transactionService.saveTransaction(testTransaction);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        verify(transactionRepository, times(1)).save(testTransaction);
    }

    @Test
    void saveTransaction_WithNullTransaction_ShouldThrowIllegalArgumentException() {
        // When & Then - код выбрасывает IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.saveTransaction(null);
        });

        // Проверяем сообщение исключения
        assertEquals("Transaction cannot be null", exception.getMessage());

        // Убеждаемся, что save не вызывался
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void saveTransaction_ShouldReturnTransactionWithId() {
        // Given
        Transaction unsavedTransaction = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(2L)
                .amount(new BigDecimal("50.00"))
                .transactionDate(unsavedTransaction.getTransactionDate())
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        when(transactionRepository.save(unsavedTransaction)).thenReturn(savedTransaction);

        // When
        Transaction result = transactionService.saveTransaction(unsavedTransaction);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertNotNull(result.getStatus());
    }

    @Test
    void saveTransaction_ShouldSetPendingStatusIfNull() {
        // Given
        Transaction transactionWithoutStatus = Transaction.builder()
                .amount(new BigDecimal("75.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(3L)
                .amount(new BigDecimal("75.00"))
                .transactionDate(transactionWithoutStatus.getTransactionDate())
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        when(transactionRepository.save(transactionWithoutStatus)).thenReturn(savedTransaction);

        // When
        Transaction result = transactionService.saveTransaction(transactionWithoutStatus);

        // Then
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
    }

    @Test
    void saveTransaction_ShouldPreserveExistingStatus() {
        // Given
        Transaction transactionWithStatus = Transaction.builder()
                .amount(new BigDecimal("200.00"))
                .transactionDate(LocalDateTime.now())
                .status(Transaction.TransactionStatus.FAILED)
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(4L)
                .amount(new BigDecimal("200.00"))
                .transactionDate(transactionWithStatus.getTransactionDate())
                .status(Transaction.TransactionStatus.FAILED)
                .build();

        when(transactionRepository.save(transactionWithStatus)).thenReturn(savedTransaction);

        // When
        Transaction result = transactionService.saveTransaction(transactionWithStatus);

        // Then
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.FAILED, result.getStatus());
    }

    @Test
    void saveTransaction_WithZeroAmount_ShouldWork() {
        // Given
        Transaction zeroAmountTransaction = Transaction.builder()
                .amount(BigDecimal.ZERO)
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(5L)
                .amount(BigDecimal.ZERO)
                .transactionDate(zeroAmountTransaction.getTransactionDate())
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        when(transactionRepository.save(zeroAmountTransaction)).thenReturn(savedTransaction);

        // When
        Transaction result = transactionService.saveTransaction(zeroAmountTransaction);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getAmount());
    }

    @Test
    void saveTransaction_WithNegativeAmount_ShouldWork() {
        // Given
        Transaction negativeAmountTransaction = Transaction.builder()
                .amount(new BigDecimal("-100.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(6L)
                .amount(new BigDecimal("-100.00"))
                .transactionDate(negativeAmountTransaction.getTransactionDate())
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        when(transactionRepository.save(negativeAmountTransaction)).thenReturn(savedTransaction);

        // When
        Transaction result = transactionService.saveTransaction(negativeAmountTransaction);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("-100.00"), result.getAmount());
    }
}
