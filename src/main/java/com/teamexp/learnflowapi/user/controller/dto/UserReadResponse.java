package com.teamexp.learnflowapi.user.controller.dto;

import com.teamexp.learnflowapi.auth.controller.dto.MembershipStatus;
import com.teamexp.learnflowapi.user.model.User;
import java.time.Instant;
import lombok.Builder;

public record UserReadResponse(
        String id,
        String nickname,
        String email,
        String role,
        boolean isMembershipActive,
        Instant membershipExpired
) {
    @Builder
    public UserReadResponse(String id, String nickname, String email, String role, boolean isMembershipActive,
                            Instant membershipExpired) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.isMembershipActive = isMembershipActive;
        this.membershipExpired = membershipExpired;
    }

    public static UserReadResponse of(User user, MembershipStatus status) {
        return UserReadResponse.builder()
                .id(user.getUserId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isMembershipActive(status.isMembershipActive())
                .membershipExpired(status.membershipExpiryDate())
                .build();
    }
}
