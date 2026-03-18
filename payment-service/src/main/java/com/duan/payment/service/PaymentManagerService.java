package com.duan.payment.service;

import com.duan.payment.dto.CoreTripResponse;
import com.duan.payment.client.CoreServiceClient;
import com.duan.payment.entity.PaymentTransaction;
import com.duan.payment.entity.TransactionStatus;
import com.duan.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentManagerService {
    private final CoreServiceClient coreClient;
    private final List<PaymentStrategy> strategies;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentProducer paymentProducer;
    
    @Value("${app.internal-api-key}")
    private String internalKey;

    public String initPayment(Long bookingId, String provider) {
        // 1. Gọi Core lấy thông tin tiền
        CoreTripResponse coreData = coreClient.getBookingInfo(bookingId, internalKey);

        // 2. Lưu transaction ở trạng thái PENDING
        PaymentTransaction tx = PaymentTransaction.builder()
                .bookingId(bookingId)
                .amount(coreData.getAmount())
                .provider(provider)
                .status(TransactionStatus.PENDING)
                .transactionRef("HDAY_" + System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        // 3. Tìm Strategy phù hợp và tạo URL
        return strategies.stream()
                .filter(s -> s.getProviderName().equalsIgnoreCase(provider))
                .findFirst()
                .map(s -> s.createPaymentUrl(bookingId, coreData.getAmount(), coreData.getDescription()))
                .orElseThrow(() -> new RuntimeException("Provider not supported"));
    }

    @Transactional
    public void processCallback(String transactionRef, String providerId, boolean isSuccess) {
       PaymentTransaction tx = transactionRepository.findByTransactionRef(transactionRef)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionRef));
        if (isSuccess) {
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setProviderTransactionId(providerId);
            // 4. Bắn tin nhắn báo cho Core-service
            paymentProducer.sendPaymentSuccess(tx.getBookingId());
        } else {
            tx.setStatus(TransactionStatus.FAILED);
        }
        transactionRepository.save(tx);
    }
}