package com.teamexp.learnflowapi.membership.service;

import com.teamexp.learnflowapi.membership.repository.MembershipRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MembershipScheduler {

    private final MembershipRepository membershipRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredMemberships() {
        membershipRepository.deleteAllByExpiredAtBefore(Instant.now());
    }
}