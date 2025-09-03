package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransferService transferService;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setOwner(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(1000));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setOwner(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.ZERO);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
    }

    @Test
    public void transfer_Success_UpdatesBalances() {
        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(1L);
        transferDto.setToCardId(2L);
        transferDto.setAmount(BigDecimal.valueOf(500));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(cardRepository.saveAll(Arrays.asList(fromCard, toCard))).thenReturn(Arrays.asList(fromCard, toCard));

        transferService.transfer(transferDto);

        assertEquals(BigDecimal.valueOf(500), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(500), toCard.getBalance());
        verify(cardRepository).saveAll(Arrays.asList(fromCard, toCard));
    }

    @Test
    public void transfer_InsufficientFunds_ThrowsException() {
        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(1L);
        transferDto.setToCardId(2L);
        transferDto.setAmount(BigDecimal.valueOf(2000));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(transferDto));
        verify(cardRepository, never()).saveAll(any());
    }

    @Test
    public void transfer_NonActiveCard_ThrowsException() {
        fromCard.setStatus(CardStatus.BLOCKED);
        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(1L);
        transferDto.setToCardId(2L);
        transferDto.setAmount(BigDecimal.valueOf(500));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(IllegalStateException.class, () -> transferService.transfer(transferDto));
        verify(cardRepository, never()).saveAll(any());
    }

    @Test
    public void transfer_NonOwnerCard_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        toCard.setOwner(otherUser);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(1L);
        transferDto.setToCardId(2L);
        transferDto.setAmount(BigDecimal.valueOf(500));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(IllegalArgumentException.class, () -> transferService.transfer(transferDto));
        verify(cardRepository, never()).saveAll(any());
    }
}