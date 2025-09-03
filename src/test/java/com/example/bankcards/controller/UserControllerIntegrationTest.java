package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreationDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setName("USER");
        roleRepository.save(role);

        User user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllUsers_Success_ReturnsUserList() throws Exception {
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("testUser"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createUser_Success_ReturnsCreatedUser() throws Exception {
        UserCreationDto creationDto = new UserCreationDto();
        creationDto.setUsername("newUser");
        creationDto.setPassword("password");
        creationDto.setEmail("new@example.com");
        creationDto.setRoles(Set.of("USER"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("newUser"));
    }
}