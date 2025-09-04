package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class TransferService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public TransferService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }
    /**
     * Transfers funds between two cards owned by the same user
     *
     * @param transferDto DTO containing transfer details
     * @throws ResourceNotFoundException if cards not found
     * @throws IllegalArgumentException if cards don't belong to current user
     * @throws IllegalStateException if cards are not in ACTIVE status
     * @throws InsufficientFundsException if insufficient funds on source card
     */
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void transfer(TransferDto transferDto) {
        Card fromCard = cardRepository.findById(transferDto.getFromCardId())
            .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferDto.getFromCardId()));
        Card toCard = cardRepository.findById(transferDto.getToCardId())
            .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferDto.getToCardId()));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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