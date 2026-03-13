package com.duan.hday.service;

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

    // Sửa lỗi: Đảm bảo trả về String thay vì void
    public String requestAiMatching(Long tripId) {
        var trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Gọi sang MatchingClient (Đảm bảo hàm này trả về String)
        return matchingClient.callAIForMatching(
            trip.getId(), 
            trip.getRoutePolyline(), 
            trip.getAvailableSeats()
        );
    }

    // Định nghĩa hàm còn thiếu để Controller không báo lỗi
    public List<?> getOptimizedMatchesFromAi(Long tripId) {
        try {
            // Sau này bạn sẽ bổ sung logic gọi gRPC sang Python 
            // để lấy danh sách ID khách đã được AI chấm điểm.
            // Hiện tại để trả về danh sách trống để code không lỗi.
            return Collections.emptyList(); 
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}