package com.teamexp.learnflowapi.membership.service.event;

import com.teamexp.learnflowapi.membership.service.MembershipService;
import com.teamexp.learnflowapi.membership.service.dto.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MembershipEventListener {

    private final MembershipService membershipService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        membershipService.activateMembership(
                event.userId(),
                event.planType()
        );
    }
}