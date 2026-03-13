package com.duan.hday.grpc.client;

import com.duan.hday.entity.PassengerTripRequest; // Đảm bảo import đúng Entity
import com.duan.hday.grpc.*; // Import tất cả class sinh ra từ proto
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchingClient {

    @GrpcClient("ai-matching")
    private MatchingServiceGrpc.MatchingServiceBlockingStub matchingStub;

    /**
     * LOGIC MỚI: Đồng bộ dữ liệu PassengerTripRequest sang Database của AI
     */
    public void syncRequestToAI(PassengerTripRequest req) {
        try {
            log.info("--- [gRPC SYNC] Đang gửi Request ID {} sang AI Service ---", req.getId());

            // 1. Tạo tọa độ
            Coordinate start = Coordinate.newBuilder()
                    .setLat(req.getStartLocation().getLat())
                    .setLng(req.getStartLocation().getLng())
                    .build();

            Coordinate end = Coordinate.newBuilder()
                    .setLat(req.getEndLocation().getLat())
                    .setLng(req.getEndLocation().getLng())
                    .build();

            // 2. Build request đồng bộ (Dựa trên file .proto bạn đã cập nhật)
            PassengerSyncRequest syncRequest = PassengerSyncRequest.newBuilder()
                    .setRequestId(req.getId())
                    .setPassengerName(req.getPassenger().getFullName())
                    .setStartLocation(start)
                    .setEndLocation(end)
                    .setDepartureTime(req.getDesiredDepartureTime().toString())
                    .setSeatsRequested(req.getSeatsRequested())
                    .build();

            // 3. Gọi hàm RPC sang Python
            SyncResponse response = matchingStub.syncPassengerRequest(syncRequest);

            if (response.getSuccess()) {
                log.info("✅ Đồng bộ thành công Request ID: {}", req.getId());
            } else {
                log.warn("⚠️ AI Service nhận dữ liệu nhưng phản hồi thất bại cho ID: {}", req.getId());
            }

        } catch (Exception e) {
            log.error("❌ Lỗi kết nối gRPC khi đồng bộ: {}", e.getMessage());
            // Không throw exception để tránh làm hỏng Transaction của luồng chính bên Core
        }
    }

    /**
     * LOGIC CŨ: Gọi AI để lấy danh sách khách cho tài xế
     */
    public String callAIForMatching(Long tripId, String polyline, int seats) {
        log.info("Đang gửi yêu cầu Matching sang AI Service cho Trip ID: {}", tripId);

        MatchRequest request = MatchRequest.newBuilder()
                .setTripId(tripId)
                .setRoutePolyline(polyline != null ? polyline : "")
                .setAvailableSeats(seats)
                .build();

        try {
            MatchResponse response = matchingStub.getPotentialPassengers(request);
            int count = response.getMatchesCount();
            log.info("AI Service đã tìm thấy {} khách hàng tiềm năng", count);
            
            if (count > 0) {
                return "Thành công: Tìm thấy " + count + " khách hàng. Người đầu tiên: " + response.getMatches(0).getPassengerName();
            }
            return "Thành công: Không có khách hàng nào phù hợp.";
            
        } catch (Exception e) {
            log.error("Lỗi kết nối gRPC khi gọi Matching: {}", e.getMessage());
            return "Lỗi: " + e.getMessage();
        }
    }
}