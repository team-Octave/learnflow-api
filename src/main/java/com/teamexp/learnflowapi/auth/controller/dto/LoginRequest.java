package com.teamexp.learnflowapi.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @Email
        @NotNull(message = "Email cannot be null")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @NotNull(message = "Password cannot be null")
        @NotBlank(message = "Password cannot be blank")
        String password
) {
}
