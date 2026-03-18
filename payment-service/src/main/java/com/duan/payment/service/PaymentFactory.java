package com.duan.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFactory {
    private final VNPayService vnPayService;
    private final MomoService momoService;

    public PaymentStrategy getStrategy(String method) {
        return switch (method.toUpperCase()) {
            case "VNPAY" -> vnPayService;
            case "MOMO" -> momoService;
            default -> throw new IllegalArgumentException("Phuong thuc thanh toan khong ho tro");
        };
    }
}