package com.duan.payment.service;

import com.duan.payment.util.VNPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayStrategy implements PaymentStrategy {

    @Value("${app.payment.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${app.payment.vnpay.secret-key}")
    private String secretKey;

    @Value("${app.payment.vnpay.url}")
    private String vnpPayUrl;

    @Value("${app.payment.vnpay.return-url}")
    private String returnUrl;

    @Override
    public String createPaymentUrl(Long bookingId, java.math.BigDecimal amount, String description) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = "HDAY_" + System.currentTimeMillis(); // Mã đơn hàng unique
        String vnp_OrderInfo = description;
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_CurrCode = "VND";

        Map<String, String> vnp_Params = new TreeMap<>(); // TreeMap để tự động sort alphabet theo yêu cầu VNPay
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount.multiply(new java.math.BigDecimal(100)).longValue()));
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1"); // Fake IP cho môi trường Dev

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        // Build Query String
        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII)).append("=")
                 .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII)).append("&");
            hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        // Remove last &
        query.deleteCharAt(query.length() - 1);
        hashData.deleteCharAt(hashData.length() - 1);

        // Tạo Secure Hash
        String vnp_SecureHash = VNPayUtil.hmacSHA512(secretKey, hashData.toString());
        return vnpPayUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    @Override
    public String getProviderName() { return "VNPAY"; }
}