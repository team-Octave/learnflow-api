package com.teamexp.learnflowapi.user.controller.dto;

public record UserReadResponse(
    String nickname,
    String email,
    String role
) {
}
