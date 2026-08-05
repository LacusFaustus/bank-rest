package com.bank.service;

import com.bank.entity.Transaction;
import com.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionReportService {

    private final TransactionRepository transactionRepository;

    public Map<String, Object> getDailyTransactionReport() {
        Map<String, Object> report = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Long totalTransactions = transactionRepository.countByTransactionDateBetween(startOfDay, endOfDay);
        BigDecimal totalAmount = transactionRepository.sumAmountByTransactionDateBetween(startOfDay, endOfDay);
        Long successfulTransactions = transactionRepository.countByStatusAndTransactionDateBetween(
                Transaction.TransactionStatus.SUCCESS, startOfDay, endOfDay);
        Long failedTransactions = transactionRepository.countByStatusAndTransactionDateBetween(
                Transaction.TransactionStatus.FAILED, startOfDay, endOfDay);

        report.put("date", today);
        report.put("totalTransactions", totalTransactions);
        report.put("successfulTransactions", successfulTransactions);
        report.put("failedTransactions", failedTransactions);
        report.put("totalAmount", totalAmount);

        return report;
    }

    public Map<String, Object> getUserTransactionStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        Long totalTransactions = transactionRepository.countByUserInLast30Days(userId, thirtyDaysAgo);
        BigDecimal totalAmount = transactionRepository.sumAmountByUserInLast30Days(userId, thirtyDaysAgo);

        stats.put("userId", userId);
        stats.put("totalTransactions", totalTransactions != null ? totalTransactions : 0L);
        stats.put("totalAmount", totalAmount);

        // Обработка null в totalAmount
        if (totalAmount == null || totalTransactions == null || totalTransactions == 0) {
            stats.put("averageAmount", BigDecimal.ZERO);
        } else {
            stats.put("averageAmount", totalAmount.divide(
                    BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP));
        }

        return stats;
    }
}
