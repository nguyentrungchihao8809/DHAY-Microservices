package com.duan.payment.controller;

import com.duan.payment.entity.PaymentRequest;
import com.duan.payment.entity.enums.PaymentStatus;
import com.duan.payment.repository.PaymentRequestRepository;
import com.duan.payment.service.PaymentFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRequestRepository repository;
    private final PaymentFactory paymentFactory;

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(
            @RequestParam Long tripId,
            @RequestParam BigDecimal amount,
            @RequestParam String method, // MOMO hoặc VNPAY
            HttpServletRequest request) {

        // 1. Tạo bản ghi PaymentRequest trong DB
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .idempotencyKey(UUID.randomUUID())
                .tripId(tripId)
                .passengerId(1L) // Demo
                .requestId(100L) // Demo
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .paymentMethod(method)
                .build();

        repository.save(paymentRequest);

        // 2. Lấy link thanh toán tương ứng
        String url = paymentFactory.getStrategy(method).createPaymentUrl(paymentRequest, request);

        return ResponseEntity.ok(url);
    }
}