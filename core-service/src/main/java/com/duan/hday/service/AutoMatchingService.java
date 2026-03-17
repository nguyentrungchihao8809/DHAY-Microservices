package com.duan.hday.service;

import com.duan.hday.entity.enums.NotificationType;
import com.duan.hday.grpc.client.MatchingClient;
import com.duan.hday.repository.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AutoMatchingService {

    private final MatchingClient matchingClient;
    private final TripRepository tripRepository;
    private final NotificationService notificationService;

    // Sửa lỗi: Đảm bảo trả về String thay vì void
    public String requestAiMatching(Long tripId) {
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Gửi thông báo cho tài xế: "Đang quét tìm khách phù hợp..."
        notificationService.sendTypedNotification(
            trip.getDriver().getId(), 
            NotificationType.MATCH_FOUND, // Bạn có thể tạo thêm type MATCHING_IN_PROGRESS nếu muốn
            java.util.Map.of("tripId", tripId.toString()),
            "Hệ thống AI"
        );

        return matchingClient.callAIForMatching(
            trip.getId(), 
            trip.getRoutePolyline(), 
            trip.getAvailableSeats()
        );
    }

    // Định nghĩa hàm còn thiếu để Controller không báo lỗi
    public List<?> getOptimizedMatchesFromAi(Long tripId) {
        try {
            // Giả sử sau khi gọi AI, bạn có list khách:
            List<?> matches = Collections.emptyList(); 
            
            if (!matches.isEmpty()) {
                var trip = tripRepository.findById(tripId).orElse(null);
                if (trip != null) {
                    // Báo cho tài xế: "Đã tìm thấy 3 khách phù hợp dọc đường!"
                    notificationService.sendMatchSuggestionToDriver(
                        trip.getDriver().getId(), 
                        tripId, 
                        matches.size()
                    );
                }
            }
            return matches;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}