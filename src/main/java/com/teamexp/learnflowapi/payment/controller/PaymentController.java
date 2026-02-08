package com.teamexp.learnflowapi.payment.controller;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.payment.dto.request.PaymentConfirmRequest;
import com.teamexp.learnflowapi.payment.dto.response.PaymentConfirmResponse;
import com.teamexp.learnflowapi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<BaseResponse<PaymentConfirmResponse>> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        PaymentConfirmResponse response = paymentService.tossConfirm(request);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
