package com.bank.dto;

import com.bank.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponseDTO {
    private Long id;
    private String maskedCardNumber;
    private String cardHolder;
    private LocalDate expiryDate;
    private BigDecimal balance;
    private Card.CardStatus status;
    private LocalDateTime createdAt;
    private Boolean blockRequested;
    private Long userId;
    private String userName;

    public static CardResponseDTO fromEntity(Card card, String maskedCardNumber) {
        if (card == null) {
            return null;
        }

        return CardResponseDTO.builder()
                .id(card.getId())
                .maskedCardNumber(maskedCardNumber)
                .cardHolder(card.getCardHolder())
                .expiryDate(card.getExpiryDate())
                .balance(card.getBalance())
                .status(card.getStatus())
                .createdAt(card.getCreatedAt())
                .blockRequested(card.getBlockRequested())
                .userId(card.getUser() != null ? card.getUser().getId() : null)
                .userName(card.getUser() != null ? card.getUser().getUsername() : null)
                .build();
    }
}
