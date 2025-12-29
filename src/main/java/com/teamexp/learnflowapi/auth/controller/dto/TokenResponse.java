package com.teamexp.learnflowapi.auth.controller.dto;

import org.springframework.http.ResponseCookie;

public record TokenResponse(
    String accessToken,
    ResponseCookie refreshToken
) {
}
