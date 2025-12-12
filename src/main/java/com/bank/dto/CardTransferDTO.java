package com.bank.dto;

import com.bank.validation.ValidCardNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardTransferDTO {

    @NotBlank(message = "From card number is required")
    private String fromCardNumber;

    @NotBlank(message = "To card number is required")
    private String toCardNumber;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Size(max = 255, message = "Description too long")
    private String description;
}
