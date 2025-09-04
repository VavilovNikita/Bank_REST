package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreationDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Users", description = "API for user management (admin only)")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @Operation(
        summary = "Get all users",
        description = "Retrieves all users in the system. Accessible only to ADMIN users"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @Operation(
        summary = "Create new user",
        description = "Creates a new user with specified roles. Accessible only to ADMIN users"
    )
    @ApiResponse(responseCode = "201", description = "User successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Role not found")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreationDto creationDto) {
        return ResponseEntity.created(null).body(userService.createUser(creationDto));
    }
}