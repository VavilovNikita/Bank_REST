package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardCreationDto {
    @NotNull
    private String number;
    @NotNull
    private Long ownerId;
    @NotNull
    private LocalDate expirationDate;
    @PositiveOrZero
    private BigDecimal balance = BigDecimal.ZERO;
}