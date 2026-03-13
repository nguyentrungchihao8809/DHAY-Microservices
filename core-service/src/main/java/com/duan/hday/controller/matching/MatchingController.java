package com.duan.hday.controller.matching;

import com.duan.hday.entity.Trip;
import com.duan.hday.repository.trip.TripRepository;
import com.duan.hday.service.AutoMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.duan.hday.grpc.client.MatchingClient;

@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
@Slf4j
public class MatchingController {

    private final AutoMatchingService matchingService;
    private final TripRepository tripRepository;
    private final MatchingClient matchingClient;


    /**
     * API 1: Yêu cầu AI quét các khách hàng tiềm năng.
     * Lưu ý: Bây giờ logic này gọi sang Python qua gRPC.
     */
    @GetMapping("/scan/{tripId}")
    public ResponseEntity<?> scanPotentialPassengers(@PathVariable Long tripId) {
        log.info("Yêu cầu quét khách hàng cho Trip ID: {}", tripId);
        
        // 1. Kiểm tra Trip tồn tại trong DB Core trước
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi (ID: " + tripId + ")"));
        
        // 2. Gọi Service xử lý qua gRPC (Kết quả trả về thường là message SUCCESS từ AI)
        String aiResponse = matchingService.requestAiMatching(tripId);
        
        return ResponseEntity.ok(Map.of(
            "status", "REQUEST_SENT",
            "message", aiResponse,
            "details", "AI Service đang xử lý dữ liệu vector..."
        ));
    }

    /**
     * API 2: Lấy kết quả tối ưu từ AI (Lấy từ bảng nhật ký AI_MATCH_HISTORY bên DB AI)
     * Vì chúng ta tách DB, Controller này sẽ gọi qua Service để AI trả về danh sách ID, 
     * sau đó Java mới map ngược lại thông tin User từ DB Core.
     */
    @GetMapping("/optimized-results/{tripId}")
    public ResponseEntity<?> getOptimizedResults(@PathVariable Long tripId) {
        log.info("Lấy kết quả tối ưu cho Trip ID: {}", tripId);
        
        // Logic mới: 
        // 1. Java gửi request sang AI: "Cho tôi danh sách kết quả cho Trip X"
        // 2. AI trả về List<Long> passenger_request_ids
        // 3. Java query DB Core: SELECT * FROM passenger_requests WHERE id IN (...)
        
        var results = matchingService.getOptimizedMatchesFromAi(tripId);
        
        return ResponseEntity.ok(Map.of(
            "tripId", tripId,
            "suggestedMatches", results
        ));
    }

    @GetMapping("/trigger/{id}")
    public String triggerAiMatching(@PathVariable Long id) {
        // 1. Lấy dữ liệu thật từ DB Core
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Trip ID: " + id));

        // 2. Gửi dữ liệu thật sang Python
        // Giả sử Trip entity có các hàm get tương ứng
        return matchingClient.callAIForMatching(
                trip.getId(), 
                trip.getRoutePolyline(), 
                trip.getAvailableSeats()
        );
    }

}