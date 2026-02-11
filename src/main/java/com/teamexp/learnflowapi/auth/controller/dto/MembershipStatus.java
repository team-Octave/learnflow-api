package com.teamexp.learnflowapi.auth.controller.dto;

import java.time.Instant;

public record MembershipStatus(
        boolean isMembershipActive,
        Instant membershipExpiryDate
) {
    public static MembershipStatus exists(Instant membershipExpiryDate){
        return new MembershipStatus(true, membershipExpiryDate);
    }

    public static MembershipStatus noneExist(){
        return new MembershipStatus(false, null);
    }
}
