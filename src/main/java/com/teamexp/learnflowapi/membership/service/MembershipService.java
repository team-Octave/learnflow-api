package com.teamexp.learnflowapi.membership.service;

import com.teamexp.learnflowapi.membership.model.Membership;
import com.teamexp.learnflowapi.membership.repository.MembershipRepository;
import com.teamexp.learnflowapi.payment.model.constant.PlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;

    public void activateMembership(String userId, PlanType planType) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime activateAt = membershipRepository.findFirstByUserIdOrderByExpiredAtDesc(userId)
                .map(last -> last.getExpiredAt().isAfter(now) ? last.getExpiredAt() : now)
                .orElse(now);

        membershipRepository.save(Membership.create(userId, planType, activateAt));
    }
}
