package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class TransferService {

    private final CardRepository cardRepository;

    public TransferService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Transactional
    public void transfer(TransferDto transferDto) {
        Card fromCard = cardRepository.findById(transferDto.getFromCardId())
            .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferDto.getFromCardId()));
        Card toCard = cardRepository.findById(transferDto.getToCardId())
            .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferDto.getToCardId()));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        // Проверка, что обе карты принадлежат текущему пользователю
        if (!fromCard.getOwner().getId().equals(user.getId()) || !toCard.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Transfers are allowed only between own cards");
        }

        // Проверка статуса карт
        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Both cards must be in ACTIVE status");
        }

        // Проверка достаточности средств
        BigDecimal amount = transferDto.getAmount();
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on card with id: " + fromCard.getId());
        }

        // Обновление балансов
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        // Сохранение обеих карт в одной транзакции
        cardRepository.saveAll(Arrays.asList(fromCard, toCard));
    }
}