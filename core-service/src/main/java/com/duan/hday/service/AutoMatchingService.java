package com.duan.hday.service;

import com.duan.hday.entity.enums.NotificationType;
import com.duan.hday.grpc.client.MatchingClient;
import com.duan.hday.repository.trip.TripRepository;
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoMatchingService {

    private final MatchingClient matchingClient;
    private final TripRepository tripRepository;
    private final PassengerTripRequestRepository tripRequestRepository;
    private final NotificationService notificationService;

    /**
     * Luồng cho KHÁCH HÀNG: Sau khi đã thanh toán (Gọi từ PaymentConsumer)
     */
    public void requestAiMatchingForRequest(Long tripRequestId) {
        var request = tripRequestRepository.findById(tripRequestId)
                .orElseThrow(() -> new RuntimeException("Passenger Trip Request not found"));

        log.info("Kích hoạt đồng bộ dữ liệu sang AI cho Request ID: {}", tripRequestId);

        // FIX LỖI: Sử dụng hàm syncRequestToAI thay vì findDriversForPassenger 
        // để khớp với phương thức bạn đã viết trong MatchingClient
        matchingClient.syncRequestToAI(request);

        // Gửi thông báo cho khách hàng
        notificationService.sendTypedNotification(
            request.getPassenger().getId(),
            NotificationType.MATCHING_IN_PROGRESS,
            java.util.Map.of("tripRequestId", tripRequestId.toString()),
            "Hệ thống H-Day"
        );
    }

    /**
     * Luồng cho TÀI XẾ: Tìm khách phù hợp cho chuyến xe của mình
     */
    public String requestAiMatching(Long tripId) {
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Đồng bộ dữ liệu chuyến đi sang AI trước khi matching
        matchingClient.syncDriverTripToAI(trip);

        return matchingClient.callAIForMatching(
            trip.getId(), 
            trip.getRoutePolyline(), 
            trip.getAvailableSeats()
        );
    }

    public List<?> getOptimizedMatchesFromAi(Long tripId) {
        try {
            // Logic lấy danh sách từ AI nếu cần xử lý thêm ở Core
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Lỗi lấy dữ liệu từ AI: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}