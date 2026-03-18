package com.duan.payment.service;

import com.duan.payment.entity.PaymentRequest;
import com.duan.payment.util.VNPayUtil; // Ta có thể dùng chung hàm băm HMAC SHA256/512
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MomoService implements PaymentStrategy {

    @Value("${payment.momo.partner-code}") private String partnerCode;
    @Value("${payment.momo.access-key}") private String accessKey;
    @Value("${payment.momo.secret-key}") private String secretKey;
    @Value("${payment.momo.endpoint}") private String endpoint;
    @Value("${payment.momo.return-url}") private String returnUrl;
    @Value("${payment.momo.notify-url}") private String notifyUrl;

    @Override
    public String createPaymentUrl(PaymentRequest payment, jakarta.servlet.http.HttpServletRequest request) {
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = payment.getIdempotencyKey().toString();
        String orderInfo = "Thanh toan chuyen di HDay #" + payment.getTripId();
        String amount = payment.getAmount().toBigInteger().toString();
        String requestType = "captureWallet";
        String extraData = ""; 

        // 1. Tạo chuỗi ký tự để băm (Signature) theo chuẩn Momo
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipAddress=" + VNPayUtil.getIpAddress(request) +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = VNPayUtil.hmacSHA512(secretKey, rawHash); // Lưu ý: Momo dùng SHA256 hoặc 512 tùy version

        // 2. Tạo Body request gửi sang Momo
        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("partnerName", "HDAY");
        body.put("storeId", "HDAY_STORE");
        body.put("requestId", requestId);
        body.put("amount", Long.parseLong(amount));
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", returnUrl);
        body.put("ipAddress", VNPayUtil.getIpAddress(request));
        body.put("lang", "vi");
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);

        // 3. Gọi POST request bằng RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.postForObject(endpoint, body, Map.class);

        return response.get("payUrl").toString();
    }
}