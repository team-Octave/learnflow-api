package com.teamexp.learnflowapi.auth.controller.dto;

public record LoginResponse(
    String accessToken
) {

    public static LoginResponse of(TokenResponse tokenResponse) {
        return new LoginResponse(
            tokenResponse.accessToken()
        );
    }
}
