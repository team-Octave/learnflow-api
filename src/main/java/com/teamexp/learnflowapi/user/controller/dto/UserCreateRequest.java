package com.teamexp.learnflowapi.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
        @Email
        @NotNull(message = "Email cannot be null")
        String email,

        @NotNull(message = "Password cannot be null")
        @NotBlank(message = "Password cannot be blank")
        String password,

        @NotNull(message = "Nickname cannot be null")
        @NotBlank(message = "password cannot be blank")
        String nickname
) {

}
