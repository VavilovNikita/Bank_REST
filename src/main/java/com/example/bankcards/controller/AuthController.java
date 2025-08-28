package com.example.bankcards.controller;

import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.ok(authService.register(registrationDto));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }
}