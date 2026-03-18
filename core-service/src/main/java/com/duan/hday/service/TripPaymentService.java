package com.duan.hday.service;

import com.duan.hday.entity.PassengerTripRequest;
import com.duan.hday.entity.enums.RequestStatus;
import com.duan.hday.exception.AppException;
import com.duan.hday.exception.ErrorCode;
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripPaymentService {

    private final PassengerTripRequestRepository tripRequestRepository;

    public PassengerTripRequest getRequestForInternal(Long requestId) {
        return tripRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND)); // Bạn có thể thêm ErrorCode.REQUEST_NOT_FOUND
    }

    @Transactional
    public void confirmRequestAfterPayment(Long requestId) {
        PassengerTripRequest request = tripRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Logic: Khi nhận được tiền, chuyển từ WAITING sang PAID
        if (request.getStatus() == RequestStatus.WAITING) {
            request.setStatus(RequestStatus.PAID);
            tripRequestRepository.save(request);
            log.info("✅ Request ID {} đã được chuyển sang trạng thái PAID", requestId);
        } else {
            log.warn("⚠️ Request ID {} không ở trạng thái WAITING (Hiện tại: {}), bỏ qua cập nhật", 
                     requestId, request.getStatus());
        }
    }
}