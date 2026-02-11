package com.teamexp.learnflowapi.payment.repository;

import com.teamexp.learnflowapi.payment.model.PaymentHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findByUserIdOrderByApprovedAtDesc(String userId);

    Optional<PaymentHistory> findById(Long paymentId);

    Boolean existsByOrderId(String orderId);

    Boolean existsByUserId(String userId);
}
