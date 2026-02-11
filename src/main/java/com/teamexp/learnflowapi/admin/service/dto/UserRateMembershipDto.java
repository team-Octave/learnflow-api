package com.teamexp.learnflowapi.admin.service.dto;

public record UserRateMembershipDto(
        Long totalUserAmount,
        Long membershipUserAmount,
        Long normalCount,
        Double normalUserRate,
        Double membershipUserRate
) {
}
