package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UserCreationDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @Email
    private String email;
    private Set<String> roles;
}