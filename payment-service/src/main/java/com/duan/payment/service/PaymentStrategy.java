package com.duan.payment.service;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String createPaymentUrl(Long bookingId, BigDecimal amount, String description);
    String getProviderName();
}