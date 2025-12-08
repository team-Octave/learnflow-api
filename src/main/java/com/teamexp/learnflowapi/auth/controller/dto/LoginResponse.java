package com.teamexp.learnflowapi.auth.controller.dto;

public record LoginResponse(
    String nickname,
    String email,
    String role,
    String accessToken,
    String refreshToken
) {
}
