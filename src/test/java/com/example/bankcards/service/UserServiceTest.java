package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreationDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;

    @BeforeEach
    public void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setRoles(Set.of(role));
    }

    @Test
    public void loadUserByUsername_Success_ReturnsUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        User result = userService.loadUserByUsername("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("testUser"));
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void getAllUsers_Success_ReturnsUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getUsername());
        assertEquals(Set.of("USER"), result.get(0).getRoles());
        verify(userRepository).findAll();
    }

    @Test
    public void createUser_Success_ReturnsUserDto() {
        UserCreationDto creationDto = new UserCreationDto();
        creationDto.setUsername("testUser");
        creationDto.setPassword("password");
        creationDto.setEmail("test@example.com");
        creationDto.setRoles(Set.of("USER"));

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(creationDto);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals(Set.of("USER"), result.getRoles());
        verify(userRepository).save(any(User.class));
    }
}