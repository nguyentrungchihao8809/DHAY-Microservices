package com.duan.payment.service;

import com.duan.payment.entity.PaymentRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentStrategy {
    // Trả về URL thanh toán cho Frontend
    String createPaymentUrl(PaymentRequest payment, HttpServletRequest request);
}