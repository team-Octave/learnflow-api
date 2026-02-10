package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.service.dto.UserRateMembershipDto;
import com.teamexp.learnflowapi.membership.repository.MembershipRepository;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackLogService {
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public UserRateMembershipDto getRateMembershipUser() {
        long totalUserCount = userRepository.countAllByDelFlagIs(false);
        long membershipCount = membershipRepository.countAllByExpiredAtAfter(Instant.now());
        long normalCount = totalUserCount - membershipCount;

        double membershipRate = totalUserCount == 0 ? 0.0 : (double) membershipCount / totalUserCount * 100;
        double normalRate = totalUserCount == 0 ? 0.0 : (double) normalCount / totalUserCount * 100;

        return new UserRateMembershipDto(
                totalUserCount,
                membershipCount,
                normalCount,
                Math.round(normalRate * 10) / 10.0,
                Math.round(membershipRate * 10) / 10.0
        );
    }
}
