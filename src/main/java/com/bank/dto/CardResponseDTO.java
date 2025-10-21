package com.bank.dto;

import com.bank.entity.Card;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CardResponseDTO {
    private Long id;
    private String cardNumber;
    private String cardHolder;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    private BigDecimal balance;
    private Card.CardStatus status;
    private String userUsername;

    public static CardResponseDTO fromEntity(Card card, String maskedCardNumber) {
        return CardResponseDTO.builder()
                .id(card.getId())
                .cardNumber(maskedCardNumber)
                .cardHolder(card.getCardHolder())
                .expiryDate(card.getExpiryDate())
                .balance(card.getBalance())
                .status(card.getStatus())
                .userUsername(card.getUser() != null ? card.getUser().getUsername() : null)
                .build();
    }
}
