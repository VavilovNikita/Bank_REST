package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private CardUtil cardUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        cardRepository.deleteAll();

        user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        userRepository.save(user);

        fromCard = new Card();
        fromCard.setNumber("1234567890123456");
        fromCard.setOwner(user);
        fromCard.setExpirationDate(LocalDate.now().plusYears(1));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        cardRepository.save(fromCard);

        toCard = new Card();
        toCard.setNumber("9876543210987654");
        toCard.setOwner(user);
        toCard.setExpirationDate(LocalDate.now().plusYears(3));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.ZERO);
        cardRepository.save(toCard);

        when(cardUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardUtil.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardUtil.mask(anyString())).thenReturn("**** **** **** 3456");
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void transfer_Success() throws Exception {
        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCard.getId());
        transferDto.setToCardId(toCard.getId());
        transferDto.setAmount(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void transfer_InsufficientFunds_ReturnsBadRequest() throws Exception {
        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCard.getId());
        transferDto.setToCardId(toCard.getId());
        transferDto.setAmount(BigDecimal.valueOf(2000));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void transfer_BlockedCard_ReturnsBadRequest() throws Exception {
        fromCard.setStatus(CardStatus.BLOCKED);
        cardRepository.save(fromCard);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCard.getId());
        transferDto.setToCardId(toCard.getId());
        transferDto.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void transfer_ExpiredCard_ReturnsBadRequest() throws Exception {
        fromCard.setExpirationDate(LocalDate.now().minusDays(1));
        cardRepository.save(fromCard);

        TransferDto transferDto = new TransferDto();
        transferDto.setFromCardId(fromCard.getId());
        transferDto.setToCardId(toCard.getId());
        transferDto.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferDto)))
            .andExpect(status().isBadRequest());
    }
}