package com.duan.hday.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.duan.hday.entity.PassengerTripRequest;
// FIX LỖI: Import enum RequestStatus vào đây
import com.duan.hday.entity.enums.RequestStatus; 
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PassengerTripRequestRepository tripRequestRepository;
    private final AutoMatchingService autoMatchingService;

    @RabbitListener(queues = "${app.rabbitmq.payment-success-queue}")
    @Transactional
    public void handlePaymentSuccess(Long tripRequestId) {
        log.info("--- [RabbitMQ] Nhận tín hiệu thanh toán thành công cho Request ID: {} ---", tripRequestId);

        try {
            // 1. Tìm Request từ Database
            PassengerTripRequest request = tripRequestRepository.findById(tripRequestId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Request ID: " + tripRequestId));

            // 2. Cập nhật trạng thái Request sang PAID (Sử dụng đúng Enum đã import)
            request.setStatus(RequestStatus.PAID); 
            tripRequestRepository.save(request);
            log.info("✅ Cập nhật trạng thái PAID cho Request ID: {}", tripRequestId);

            // 3. Kích hoạt AI Matching dựa trên Request này (Luồng gRPC sang AI Service)
            autoMatchingService.requestAiMatchingForRequest(tripRequestId);
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý Payment Success Event: {}", e.getMessage());
            // Có thể ném lại Exception để RabbitMQ thực hiện retry nếu cần
            throw e; 
        }
    }
}