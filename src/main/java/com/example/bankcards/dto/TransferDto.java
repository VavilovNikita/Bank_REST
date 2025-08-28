package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferDto {
    @NotNull
    private Long fromCardId;
    @NotNull
    private Long toCardId;
    @Positive
    private BigDecimal amount;
}