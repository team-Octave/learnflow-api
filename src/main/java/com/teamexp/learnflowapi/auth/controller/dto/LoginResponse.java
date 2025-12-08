package com.teamexp.learnflowapi.auth.controller.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
