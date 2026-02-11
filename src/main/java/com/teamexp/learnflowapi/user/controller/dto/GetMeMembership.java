package com.teamexp.learnflowapi.user.controller.dto;

import java.time.Instant;

public record GetMeMembership(
        boolean isMembershipActive,
        Instant membershipExpiryDate
) {
    public static GetMeMembership exists(Instant membershipExpiryDate){
        return new GetMeMembership(true, membershipExpiryDate);
    }

    public static GetMeMembership noneExist(){
        return new GetMeMembership(false, null);
    }
}
