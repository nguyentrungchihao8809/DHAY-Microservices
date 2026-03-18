package com.duan.payment.controller;

import com.duan.payment.service.PaymentManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentManagerService paymentManager;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestParam Long bookingId, @RequestParam String provider) {
        return ResponseEntity.ok(paymentManager.initPayment(bookingId, provider));
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<String> vnpayCallback(HttpServletRequest request) {
        // Logic: Kiểm tra checksum từ VNPay trả về
        // Nếu OK:
        paymentManager.processCallback(request.getParameter("vnp_TxnRef"), "VNP_ID", true);
        return ResponseEntity.ok("Payment Success. Please return to app.");
    }
}