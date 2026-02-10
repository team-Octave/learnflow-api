package com.teamexp.learnflowapi.auth.controller.dto;

import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import java.time.Instant;

public record LoginResponse(
        String userId,
        String nickname,
        String email,
        String role,
        String accessToken,
        String refreshToken,
        boolean isMembershipActive,
        Instant membershipExpiryDate
) {

    public static LoginResponse create(CustomUserPrincipal user, String accessToken, String refreshToken, MembershipStatus status) {
        return new LoginResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                refreshToken,
                status.isMembershipActive(),
                status.membershipExpiryDate()
        );
    }
}
