package com.bank.event;

import com.bank.entity.Card;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CardBlockRequestedEvent extends ApplicationEvent {
    private final Card card;
    private final String reason;

    public CardBlockRequestedEvent(Object source, Card card, String reason) {
        super(source);
        this.card = card;
        this.reason = reason;
    }
}
