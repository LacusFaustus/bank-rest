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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionReportService transactionReportService;

    @BeforeEach
    void setUp() {
        // Ничего не нужно инициализировать
    }

    @Test
    void getDailyTransactionReport_ShouldReturnCompleteReport() {
        // Given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        when(transactionRepository.countByTransactionDateBetween(eq(todayStart), eq(todayEnd)))
                .thenReturn(25L);

        when(transactionRepository.sumAmountByTransactionDateBetween(eq(todayStart), eq(todayEnd)))
                .thenReturn(new BigDecimal("15000.50"));

        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.SUCCESS), eq(todayStart), eq(todayEnd)))
                .thenReturn(22L);

        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.FAILED), eq(todayStart), eq(todayEnd)))
                .thenReturn(3L);

        // When
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();

        // Then
        assertNotNull(report);
        assertEquals(5, report.size());
        assertEquals(25L, report.get("totalTransactions"));
        assertEquals(new BigDecimal("15000.50"), report.get("totalAmount"));
        assertEquals(22L, report.get("successfulTransactions"));
        assertEquals(3L, report.get("failedTransactions"));
        assertEquals(LocalDate.now(), report.get("date"));

        verify(transactionRepository, times(1)).countByTransactionDateBetween(todayStart, todayEnd);
        verify(transactionRepository, times(1)).sumAmountByTransactionDateBetween(todayStart, todayEnd);
        verify(transactionRepository, times(1))
                .countByStatusAndTransactionDateBetween(eq(Transaction.TransactionStatus.SUCCESS), eq(todayStart), eq(todayEnd));
        verify(transactionRepository, times(1))
                .countByStatusAndTransactionDateBetween(eq(Transaction.TransactionStatus.FAILED), eq(todayStart), eq(todayEnd));
    }

    @Test
    void getDailyTransactionReport_WithZeroTransactions_ShouldReturnEmptyReport() {
        // Given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        when(transactionRepository.countByTransactionDateBetween(any(), any())).thenReturn(0L);
        when(transactionRepository.sumAmountByTransactionDateBetween(any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countByStatusAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(0L);

        // When
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();

        // Then
        assertNotNull(report);
        assertEquals(0L, report.get("totalTransactions"));
        assertEquals(BigDecimal.ZERO, report.get("totalAmount"));
        assertEquals(0L, report.get("successfulTransactions"));
        assertEquals(0L, report.get("failedTransactions"));
        assertEquals(LocalDate.now(), report.get("date"));
    }

    @Test
    void getDailyTransactionReport_RepositoryReturnsNullAmount_ShouldHandleGracefully() {
        // Given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        when(transactionRepository.countByTransactionDateBetween(any(), any())).thenReturn(5L);
        when(transactionRepository.sumAmountByTransactionDateBetween(any(), any()))
                .thenReturn(null); // Симулируем null возврат
        when(transactionRepository.countByStatusAndTransactionDateBetween(any(), any(), any()))
                .thenReturn(0L);

        // When
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();

        // Then
        assertNotNull(report);
        assertEquals(5L, report.get("totalTransactions"));
        assertNull(report.get("totalAmount")); // null сохраняется
    }

    @Test
    void getDailyTransactionReport_WithOnlySuccessfulTransactions() {
        // Given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        when(transactionRepository.countByTransactionDateBetween(any(), any())).thenReturn(10L);
        when(transactionRepository.sumAmountByTransactionDateBetween(any(), any()))
                .thenReturn(new BigDecimal("5000.00"));
        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.SUCCESS), any(), any()))
                .thenReturn(10L);
        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.FAILED), any(), any()))
                .thenReturn(0L);

        // When
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();

        // Then
        assertNotNull(report);
        assertEquals(10L, report.get("totalTransactions"));
        assertEquals(10L, report.get("successfulTransactions"));
        assertEquals(0L, report.get("failedTransactions"));
    }

    @Test
    void getDailyTransactionReport_WithOnlyFailedTransactions() {
        // Given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        when(transactionRepository.countByTransactionDateBetween(any(), any())).thenReturn(5L);
        when(transactionRepository.sumAmountByTransactionDateBetween(any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.SUCCESS), any(), any()))
                .thenReturn(0L);
        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.FAILED), any(), any()))
                .thenReturn(5L);

        // When
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();

        // Then
        assertNotNull(report);
        assertEquals(5L, report.get("totalTransactions"));
        assertEquals(0L, report.get("successfulTransactions"));
        assertEquals(5L, report.get("failedTransactions"));
    }

    @Test
    void getUserTransactionStats_ShouldReturnUserStatistics() {
        // Given
        Long userId = 1L;

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(15L);

        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("7500.75"));

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        assertEquals(4, stats.size());
        assertEquals(userId, stats.get("userId"));
        assertEquals(15L, stats.get("totalTransactions"));
        assertEquals(new BigDecimal("7500.75"), stats.get("totalAmount"));

        BigDecimal expectedAverage = new BigDecimal("7500.75")
                .divide(BigDecimal.valueOf(15), 2, RoundingMode.HALF_UP);
        assertEquals(0, expectedAverage.compareTo((BigDecimal) stats.get("averageAmount")));

        verify(transactionRepository, times(1)).countByUserInLast30Days(eq(userId), any(LocalDateTime.class));
        verify(transactionRepository, times(1)).sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void getUserTransactionStats_WithNoTransactions_ShouldReturnZeroStats() {
        // Given
        Long userId = 999L;

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(0L);

        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        assertEquals(userId, stats.get("userId"));
        assertEquals(0L, stats.get("totalTransactions"));
        assertEquals(BigDecimal.ZERO, stats.get("totalAmount"));
        assertEquals(BigDecimal.ZERO, stats.get("averageAmount"));
    }

    @Test
    void getUserTransactionStats_WithOneTransaction_ShouldReturnCorrectAverage() {
        // Given
        Long userId = 1L;
        BigDecimal totalAmount = new BigDecimal("123.45");

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(1L);
        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(totalAmount);

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        assertEquals(0, totalAmount.compareTo((BigDecimal) stats.get("averageAmount")));
    }

    @Test
    void getUserTransactionStats_WithNullTotalAmount_ShouldHandleGracefully() {
        // Given
        Long userId = 1L;

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(null); // null сумма

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        assertEquals(userId, stats.get("userId"));
        assertEquals(5L, stats.get("totalTransactions"));
        assertNull(stats.get("totalAmount"));
        // averageAmount должно быть null или BigDecimal.ZERO в зависимости от реализации
        // Проверим, что метод не падает
        assertNotNull(stats.get("averageAmount"));
    }

    @Test
    void getUserTransactionStats_WithZeroTotalAmount_ShouldReturnZeroAverage() {
        // Given
        Long userId = 1L;

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(10L);
        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) stats.get("averageAmount")));
    }

    @Test
    void getUserTransactionStats_WithNullUserId_ShouldHandleGracefully() {
        // Given
        Long userId = null;

        // Когда userId null, репозиторий может вернуть 0
        when(transactionRepository.countByUserInLast30Days(isNull(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(transactionRepository.sumAmountByUserInLast30Days(isNull(), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        assertNull(stats.get("userId"));
        assertEquals(0L, stats.get("totalTransactions"));
        assertEquals(BigDecimal.ZERO, stats.get("totalAmount"));
        assertEquals(BigDecimal.ZERO, stats.get("averageAmount"));
    }

    @Test
    void getUserTransactionStats_WithLargeNumbers_ShouldCalculateCorrectly() {
        // Given
        Long userId = 1L;
        BigDecimal largeAmount = new BigDecimal("9999999.99");
        Long largeCount = 9999L;

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(largeCount);
        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(largeAmount);

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);

        BigDecimal expectedAverage = largeAmount.divide(
                BigDecimal.valueOf(largeCount), 2, RoundingMode.HALF_UP);
        assertEquals(0, expectedAverage.compareTo((BigDecimal) stats.get("averageAmount")));
    }

    @Test
    void getUserTransactionStats_WithPreciseDivision_ShouldRoundCorrectly() {
        // Given
        Long userId = 1L;
        BigDecimal totalAmount = new BigDecimal("100.00");
        Long transactionCount = 3L; // 100 / 3 = 33.333... -> 33.33

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(transactionCount);
        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(totalAmount);

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        BigDecimal average = (BigDecimal) stats.get("averageAmount");
        assertEquals(0, new BigDecimal("33.33").compareTo(average));
    }

    @Test
    void getDailyTransactionReport_WithMixedStatuses() {
        // Given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        when(transactionRepository.countByTransactionDateBetween(any(), any())).thenReturn(100L);
        when(transactionRepository.sumAmountByTransactionDateBetween(any(), any()))
                .thenReturn(new BigDecimal("50000.00"));
        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.SUCCESS), any(), any()))
                .thenReturn(95L);
        when(transactionRepository.countByStatusAndTransactionDateBetween(
                eq(Transaction.TransactionStatus.FAILED), any(), any()))
                .thenReturn(5L);

        // When
        Map<String, Object> report = transactionReportService.getDailyTransactionReport();

        // Then
        assertNotNull(report);
        assertEquals(100L, report.get("totalTransactions"));
        assertEquals(95L, report.get("successfulTransactions"));
        assertEquals(5L, report.get("failedTransactions"));
    }

    @Test
    void getUserTransactionStats_WhenTotalAmountIsNull_ShouldReturnZeroAverage() {
        // Given
        Long userId = 1L;

        when(transactionRepository.countByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(transactionRepository.sumAmountByUserInLast30Days(eq(userId), any(LocalDateTime.class)))
                .thenReturn(null);

        // When
        Map<String, Object> stats = transactionReportService.getUserTransactionStats(userId);

        // Then
        assertNotNull(stats);
        // Используем assertSame для сравнения BigDecimal.ZERO
        assertSame(BigDecimal.ZERO, stats.get("averageAmount"));
    }
}
