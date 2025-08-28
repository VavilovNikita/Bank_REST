package com.example.bankcards.service;

import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public Map<String, String> register(UserRegistrationDto registrationDto) {
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        Role userRole = roleRepository.findByName("USER").orElseThrow();
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    public Map<String, String> login(UserLoginDto loginDto) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );
        User user = userRepository.findByUsername(loginDto.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }
}