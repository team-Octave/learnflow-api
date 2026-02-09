package com.teamexp.learnflowapi.membership.repository;

import com.teamexp.learnflowapi.membership.model.Membership;
import com.teamexp.learnflowapi.membership.model.constant.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUserIdAndStatus(String userId, MembershipStatus status);

    boolean existsByUserIdAndStatus(String userId, MembershipStatus status);

    Optional<Membership> findByOrderId(String orderId);

    List<Membership> findAllByUserIdOrderByCreatedAtDesc(String userId);
}
