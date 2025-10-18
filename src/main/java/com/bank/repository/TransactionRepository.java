package com.bank.repository;

import com.bank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.fromCard.user.id = :userId OR t.toCard.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end")
    long countByTransactionDateBetween(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status AND t.transactionDate BETWEEN :start AND :end")
    long countByStatusAndTransactionDateBetween(@Param("status") Transaction.TransactionStatus status,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumAmountByTransactionDateBetween(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
            "(t.fromCard.user.id = :userId OR t.toCard.user.id = :userId) AND " +
            "t.transactionDate >= :since")
    long countByUserInLast30Days(@Param("userId") Long userId,
                                 @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE " +
            "(t.fromCard.user.id = :userId OR t.toCard.user.id = :userId) AND " +
            "t.transactionDate >= :since")
    BigDecimal sumAmountByUserInLast30Days(@Param("userId") Long userId,
                                           @Param("since") LocalDateTime since);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.fromCard.user.id = :userId OR t.toCard.user.id = :userId) " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactionsByUser(@Param("userId") Long userId);
}
