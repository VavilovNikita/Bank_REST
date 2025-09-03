package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreationDto;
import com.example.bankcards.dto.CardUpdateDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CardControllerIntegrationTest {

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
    private Card card;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        cardRepository.deleteAll();

        user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        userRepository.save(user);

        card = new Card();
        card.setNumber("1234567890123456");
        card.setOwner(user);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000));
        cardRepository.save(card);

        when(cardUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardUtil.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void getCards_Success_ReturnsCardList() throws Exception {
        mockMvc.perform(get("/api/cards")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(card.getId()))
            .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createCard_Success_ReturnsCreatedCard() throws Exception {
        CardCreationDto creationDto = new CardCreationDto();
        creationDto.setNumber("1234567890123456");
        creationDto.setOwnerId(user.getId());
        creationDto.setExpirationDate(LocalDate.now().plusYears(3));
        creationDto.setBalance(BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void getCardById_Success_ReturnsCard() throws Exception {
        mockMvc.perform(get("/api/cards/" + card.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(card.getId()))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void updateCard_Success_ReturnsUpdatedCard() throws Exception {
        CardUpdateDto updateDto = new CardUpdateDto();
        updateDto.setExpirationDate(LocalDate.now().plusYears(2));
        updateDto.setStatus("BLOCKED");

        mockMvc.perform(put("/api/cards/" + card.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void deleteCard_Success() throws Exception {
        mockMvc.perform(delete("/api/cards/" + card.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void blockCard_Success() throws Exception {
        mockMvc.perform(post("/api/cards/" + card.getId() + "/block")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void activateCard_Success() throws Exception {
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        mockMvc.perform(post("/api/cards/" + card.getId() + "/activate")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}