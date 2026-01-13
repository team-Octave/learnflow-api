package com.teamexp.learnflowapi.auth.controller.dto;

public record ReissuanceResponse(
    String accessToken,
    String refreshToken
) {
}
