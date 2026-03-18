package com.duan.hday.controller.payment;

import com.duan.hday.dto.response.payment.PaymentInfoResponse;
import com.duan.hday.entity.PassengerTripRequest; // Đổi từ Booking sang Request
import com.duan.hday.service.TripPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
// Đổi path từ /bookings sang /requests để đúng ý nghĩa nghiệp vụ mới
@RequestMapping("/api/v1/internal/trip-requests") 
@RequiredArgsConstructor
public class InternalTripController {

    private final TripPaymentService tripPaymentService;

    @Value("${app.internal-api-key}")
    private String internalApiKey;

    @GetMapping("/{id}/payment-info")
    public ResponseEntity<PaymentInfoResponse> getPaymentInfo(
            @PathVariable Long id,
            @RequestHeader("X-Internal-Key") String apiKey) {

        // 1. Kiểm tra bảo mật API Key nội bộ
        if (!internalApiKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Internal Key");
        }

        // 2. Gọi hàm đã sửa trong TripPaymentService (getRequestForInternal)
        PassengerTripRequest request = tripPaymentService.getRequestForInternal(id);

        // 3. Map sang PaymentInfoResponse (Dùng dữ liệu từ Request)
        return ResponseEntity.ok(PaymentInfoResponse.builder()
                .bookingId(request.getId()) // Vẫn trả về ID này để Payment-service xử lý
                .amount(request.getEstimatedPrice()) // Giá dự kiến từ Request
                .currency("VND")
                .status(request.getStatus().name())
                .description("H-Day: Thanh toán yêu cầu đặt xe #" + request.getId())
                .build());
    }
}