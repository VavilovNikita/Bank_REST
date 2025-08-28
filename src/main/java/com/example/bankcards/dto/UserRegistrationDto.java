package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @Email
    private String email;
}