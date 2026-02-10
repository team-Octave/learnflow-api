package com.teamexp.learnflowapi.membership.repository;

import com.teamexp.learnflowapi.membership.model.Membership;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Optional<Membership> findFirstByUserIdOrderByExpiredAtDesc(String userId);

    void deleteAllByExpiredAtBefore(Instant expiredAt);
}
