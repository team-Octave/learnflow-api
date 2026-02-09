package com.teamexp.learnflowapi.payment.controller;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.payment.dto.PaymentQueryResponse;
import com.teamexp.learnflowapi.payment.dto.request.PaymentConfirmRequest;
import com.teamexp.learnflowapi.payment.dto.response.PaymentConfirmResponse;
import com.teamexp.learnflowapi.payment.service.PaymentHistoryService;
import com.teamexp.learnflowapi.payment.service.PaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentHistoryService paymentHistoryService;

    @PostMapping("/confirm")
    public ResponseEntity<BaseResponse<PaymentConfirmResponse>> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
            ) {
        PaymentConfirmResponse response = paymentService.tossConfirm(request, principal.getId());
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @GetMapping("/history")
    public ResponseEntity<BaseResponse<List<PaymentQueryResponse>>> getPaymentHistories(@AuthenticationPrincipal CustomUserPrincipal principal){
        List<PaymentQueryResponse> response = paymentHistoryService.getAllPaymentHistoryByUserId(principal.getId())
                .stream()
                .map(PaymentQueryResponse::of)
                .toList();
        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
