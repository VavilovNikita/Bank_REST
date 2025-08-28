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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardUtil cardUtil;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CardUtil cardUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardUtil = cardUtil;
    }

    public Page<CardDto> getCards(int page, int size, String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        Page<Card> cards;
        if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            if (status != null) {
                cards = cardRepository.findByStatus(CardStatus.valueOf(status.toUpperCase()), pageable);
            } else {
                cards = cardRepository.findAll(pageable);
            }
        } else {
            if (status != null) {
                cards = cardRepository.findByOwnerIdAndStatus(user.getId(), CardStatus.valueOf(status.toUpperCase()), pageable);
            } else {
                cards = cardRepository.findByOwnerId(user.getId(), pageable);
            }
        }
        return cards.map(this::mapToDto);
    }

    public CardDto createCard(CardCreationDto creationDto) {
        Card card = new Card();
        card.setNumber(cardUtil.encrypt(creationDto.getNumber()));
        User owner = userRepository.findById(creationDto.getOwnerId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        card.setOwner(owner);
        card.setExpirationDate(creationDto.getExpirationDate());
        card.setBalance(creationDto.getBalance());
        cardRepository.save(card);
        return mapToDto(card);
    }

    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        checkOwnership(card);
        return mapToDto(card);
    }

    public CardDto updateCard(Long id, CardUpdateDto updateDto) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        card.setExpirationDate(updateDto.getExpirationDate());
        if (updateDto.getStatus() != null) {
            card.setStatus(CardStatus.valueOf(updateDto.getStatus().toUpperCase()));
        }
        cardRepository.save(card);
        return mapToDto(card);
    }

    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        cardRepository.delete(card);
    }

    public void blockCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        checkOwnership(card);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public void activateCard(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    private void checkOwnership(Card card) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        if (!user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) && !card.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Card not found");
        }
    }

    private CardDto mapToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedNumber(cardUtil.mask(card.getNumber()));
        dto.setOwnerId(card.getOwner().getId());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus().name());
        dto.setBalance(card.getBalance());
        return dto;
    }
}