package com.example.bankcards.service;

import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

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
    public void register_Success_ReturnsToken() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testUser");
        registrationDto.setPassword("password");
        registrationDto.setEmail("test@example.com");

        User userToSave = new User();
        userToSave.setUsername("testUser");
        userToSave.setPassword("encodedPassword");
        userToSave.setEmail("test@example.com");
        userToSave.setRoles(Set.of(role));

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testUser");
        savedUser.setPassword("encodedPassword");
        savedUser.setEmail("test@example.com");
        savedUser.setRoles(Set.of(role));

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwtToken");

        Map<String, String> result = authService.register(registrationDto);

        assertNotNull(result);
        assertEquals("jwtToken", result.get("token"));
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    public void login_Success_ReturnsToken() {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("testUser");
        loginDto.setPassword("password");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        Map<String, String> result = authService.login(loginDto);

        assertNotNull(result);
        assertEquals("jwtToken", result.get("token"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    public void login_UserNotFound_ThrowsException() {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("testUser");
        loginDto.setPassword("password");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.login(loginDto));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}