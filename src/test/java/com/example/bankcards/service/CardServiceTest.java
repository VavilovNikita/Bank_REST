package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreationDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardUtil cardUtil;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        card = new Card();
        card.setId(1L);
        card.setNumber("encryptedNumber");
        card.setOwner(user);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void getCards_UserRole_ReturnsUserCards() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = Collections.singletonList(card);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, 1);

        when(cardRepository.findByOwnerId(1L, pageable)).thenReturn(cardPage);
        when(cardUtil.mask("encryptedNumber")).thenReturn("**** **** **** 3456");

        Page<CardDto> result = cardService.getCards(0, 10, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("**** **** **** 3456", result.getContent().get(0).getMaskedNumber());
        verify(cardRepository).findByOwnerId(1L, pageable);
    }

    @Test
    public void getCards_AdminRoleWithStatus_ReturnsFilteredCards() {
        user.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = Collections.singletonList(card);
        Page<Card> cardPage = new PageImpl<>(cards, pageable, 1);

        when(cardRepository.findByStatus(CardStatus.ACTIVE, pageable)).thenReturn(cardPage);
        when(cardUtil.mask("encryptedNumber")).thenReturn("**** **** **** 3456");

        Page<CardDto> result = cardService.getCards(0, 10, "ACTIVE");

        assertEquals(1, result.getTotalElements());
        assertEquals("ACTIVE", result.getContent().get(0).getStatus());
        verify(cardRepository).findByStatus(CardStatus.ACTIVE, pageable);
    }

    @Test
    public void getCardById_Success_ReturnsCardDto() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardUtil.mask("encryptedNumber")).thenReturn("**** **** **** 3456");

        CardDto result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getMaskedNumber());
        verify(cardRepository).findById(1L);
    }

    @Test
    public void getCardById_NonOwnerUser_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");
        otherUser.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("otherUser");
        when(userRepository.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(ResourceNotFoundException.class, () -> cardService.getCardById(1L));
        verify(cardRepository).findById(1L);
    }

    @Test
    public void blockCard_Success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        cardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }
    @Test
    public void createCard_Success_ReturnsCardDto() {
        CardCreationDto creationDto = new CardCreationDto();
        creationDto.setNumber("1234567890123456");
        creationDto.setOwnerId(1L);
        creationDto.setExpirationDate(LocalDate.now().plusYears(3));
        creationDto.setBalance(BigDecimal.valueOf(1000));

        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setNumber("encryptedNumber");
        savedCard.setOwner(user);
        savedCard.setExpirationDate(LocalDate.now().plusYears(3));
        savedCard.setStatus(CardStatus.ACTIVE);
        savedCard.setBalance(BigDecimal.valueOf(1000));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardUtil.encrypt("1234567890123456")).thenReturn("encryptedNumber");
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(cardUtil.mask("encryptedNumber")).thenReturn("**** **** **** 3456");

        CardDto result = cardService.createCard(creationDto);

        assertNotNull(result);
        assertEquals(1L, result.getOwnerId());
        assertEquals("**** **** **** 3456", result.getMaskedNumber());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    public void getCardById_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.getCardById(1L));
        verify(cardRepository).findById(1L);
    }

    @Test
    public void updateCard_Success_ReturnsUpdatedCardDto() {
        CardUpdateDto updateDto = new CardUpdateDto();
        updateDto.setExpirationDate(LocalDate.now().plusYears(2));
        updateDto.setStatus("BLOCKED");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardUtil.mask("encryptedNumber")).thenReturn("**** **** **** 3456");

        CardDto result = cardService.updateCard(1L, updateDto);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED.name(), result.getStatus());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    public void deleteCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card);
    }

    @Test
    public void activateCard_Success() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        cardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }
}