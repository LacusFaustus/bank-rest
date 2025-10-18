package com.bank.service;

import com.bank.entity.Transaction;
import com.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionReportService transactionReportService;

    @BeforeEach
    void setUp() {
        transactionReportService = new TransactionReportService(transactionRepository);
    }

    @Test
    void getDailyTransactionReport_ShouldReturnReport() {
        // Given
        when(transactionRepository.countByTransactionDateBetween(any(), any())).thenReturn(10L);
        when(transactionRepository.sumAmountByTransactionDateBetween(any(), any())).thenReturn(new BigDecimal("1000.00"));
        when(transactionRepository.countByStatusAndTransactionDateBetween(any(), any(), any())).thenReturn(8L);

        // When
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();

        // Then
        assertNotNull(report);
        assertEquals(10L, report.get("totalTransactions"));
        assertEquals(8L, report.get("successfulTransactions"));
        assertEquals(2L, report.get("failedTransactions"));
        assertEquals(new BigDecimal("1000.00"), report.get("totalAmount"));
    }

    @Test
    void getUserTransactionStats_ShouldReturnStats() {
        // Given
        when(transactionRepository.countByUserInLast30Days(any(), any())).thenReturn(5L);
        when(transactionRepository.sumAmountByUserInLast30Days(any(), any())).thenReturn(new BigDecimal("500.00"));

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(1L, stats.get("userId"));
        assertEquals(5L, stats.get("totalTransactions"));
        assertEquals(new BigDecimal("500.00"), stats.get("totalAmount"));
        assertEquals(new BigDecimal("100.00"), stats.get("averageAmount"));
    }
}
