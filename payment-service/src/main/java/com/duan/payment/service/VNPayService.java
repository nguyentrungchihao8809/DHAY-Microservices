package com.duan.payment.service; // <--- Cực kỳ quan trọng

import com.duan.payment.entity.PaymentRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class VNPayService implements PaymentStrategy { // Thêm implements nếu bạn dùng Strategy

    @Value("${payment.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${payment.vnpay.hash-secret}")
    private String hashSecret;

    @Value("${payment.vnpay.url}")
    private String vnpUrl;

    @Value("${payment.vnpay.return-url}")
    private String returnUrl;

    @Override
    public String createPaymentUrl(PaymentRequest payment, HttpServletRequest request) {
        // ... (Giữ nguyên logic tạo URL tôi đã cung cấp ở bước trước)
        return "https://sandbox.vnpayment.vn/..."; 
    }
}