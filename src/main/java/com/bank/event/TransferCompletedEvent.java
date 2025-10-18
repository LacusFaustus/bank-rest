package com.bank.event;

import com.bank.entity.Card;
import com.bank.entity.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class TransferCompletedEvent extends ApplicationEvent {
    private final Transaction transaction;
    private final Card fromCard;
    private final Card toCard;
    private final BigDecimal amount;

    public TransferCompletedEvent(Object source, Transaction transaction, Card fromCard, Card toCard, BigDecimal amount) {
        super(source);
        this.transaction = transaction;
        this.fromCard = fromCard;
        this.toCard = toCard;
        this.amount = amount;
    }
}
