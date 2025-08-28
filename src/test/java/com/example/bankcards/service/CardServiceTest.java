package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardUtil cardUtil;

    @InjectMocks
    private CardService cardService;

    @Test
    public void deleteCard() {
        Long id = 1L;
        Card card = new Card();
        org.mockito.BDDMockito.given(cardRepository.findById(id)).willReturn(java.util.Optional.of(card));

        cardService.deleteCard(id);

        verify(cardRepository).delete(card);
    }

    // Add more tests
}