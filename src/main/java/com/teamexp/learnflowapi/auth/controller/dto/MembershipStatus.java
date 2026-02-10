package com.teamexp.learnflowapi.auth.controller.dto;

import java.time.Instant;

public record MembershipStatus(
        boolean isMembershipActive,
        Instant membershipExpiryDate
) {
}
