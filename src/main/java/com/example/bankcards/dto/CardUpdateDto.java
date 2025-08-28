package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardUpdateDto {
    @NotNull
    private LocalDate expirationDate;
    private String status;
}