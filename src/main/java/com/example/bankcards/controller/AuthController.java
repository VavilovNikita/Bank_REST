package com.example.bankcards.controller;

import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "Authentication", description = "API for user authentication and registration")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Register new user",
        description = "Registers a new user account with default USER role. Public endpoint - no authentication required"
    )
    @ApiResponse(responseCode = "200", description = "Authentication token response")
    @ApiResponse(responseCode = "400", description = "Invalid registration data")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.ok(authService.register(registrationDto));
    }
    @Operation(
        summary = "Authenticate user",
        description = "Validates user credentials and returns JWT token. Public endpoint - no authentication required"
    )
    @ApiResponse(responseCode = "200", description = "Authentication token response")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }
}