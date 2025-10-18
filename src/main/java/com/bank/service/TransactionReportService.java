package com.bank.service;

import com.bank.entity.Transaction;
import com.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionReportService {

    private final TransactionRepository transactionRepository;

    public Map<String, Object> getDailyTransactionReport() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        long totalTransactions = transactionRepository.countByTransactionDateBetween(startOfDay, endOfDay);
        BigDecimal totalAmount = transactionRepository.sumAmountByTransactionDateBetween(startOfDay, endOfDay);
        long successfulTransactions = transactionRepository.countByStatusAndTransactionDateBetween(
                Transaction.TransactionStatus.SUCCESS, startOfDay, endOfDay);

        Map<String, Object> report = new HashMap<>();
        report.put("date", LocalDateTime.now().toLocalDate());
        report.put("totalTransactions", totalTransactions);
        report.put("successfulTransactions", successfulTransactions);
        report.put("failedTransactions", totalTransactions - successfulTransactions);
        report.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        report.put("successRate", totalTransactions > 0 ?
                BigDecimal.valueOf((double) successfulTransactions / totalTransactions * 100)
                        .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        return report;
    }

    public Map<String, Object> getUserTransactionStats(Long userId) {
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);

        long userTransactions = transactionRepository.countByUserInLast30Days(userId, last30Days);
        BigDecimal userTotalAmount = transactionRepository.sumAmountByUserInLast30Days(userId, last30Days);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("period", "LAST_30_DAYS");
        stats.put("totalTransactions", userTransactions);
        stats.put("totalAmount", userTotalAmount != null ? userTotalAmount : BigDecimal.ZERO);
        stats.put("averageAmount", userTransactions > 0 ?
                userTotalAmount.divide(BigDecimal.valueOf(userTransactions), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        return stats;
    }
}
