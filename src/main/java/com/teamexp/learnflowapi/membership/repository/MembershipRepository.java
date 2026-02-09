package com.teamexp.learnflowapi.membership.repository;

import com.teamexp.learnflowapi.membership.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Optional<Membership> findFirstByUserIdOrderByExpiredAtDesc(String userId);

    void deleteAllByExpiredAtBefore(LocalDateTime expiredAt);
}
