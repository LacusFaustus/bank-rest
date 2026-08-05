package com.bank.service;

import com.bank.entity.Transaction;
import com.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        // Устанавливаем статус по умолчанию, если не установлен
        if (transaction.getStatus() == null) {
            transaction.setStatus(Transaction.TransactionStatus.PENDING);
        }

        return transactionRepository.save(transaction);
    }
}
