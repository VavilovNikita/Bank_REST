package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreationDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreationDto creationDto) {
        return ResponseEntity.created(null).body(userService.createUser(creationDto));
    }

    // Additional endpoints if needed
}