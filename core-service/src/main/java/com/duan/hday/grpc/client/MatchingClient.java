package com.duan.hday.grpc.client;

import com.duan.hday.grpc.MatchingServiceGrpc;
import com.duan.hday.grpc.MatchRequest;
import com.duan.hday.grpc.MatchResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchingClient {

    @GrpcClient("ai-matching") // Đã khớp với properties
    private MatchingServiceGrpc.MatchingServiceBlockingStub matchingStub;

    public String callAIForMatching(Long tripId, String polyline, int seats) {
        log.info("Đang gửi yêu cầu Matching sang AI Service cho Trip ID: {}", tripId);

        MatchRequest request = MatchRequest.newBuilder()
                .setTripId(tripId)
                .setRoutePolyline(polyline)
                .setAvailableSeats(seats) // Lưu ý: Proto là available_seats -> Java là setAvailableSeats
                .build();

        try {
            MatchResponse response = matchingStub.getPotentialPassengers(request);
            
            // Lấy danh sách kết quả
            int count = response.getMatchesCount();
            log.info("AI Service đã tìm thấy {} khách hàng tiềm năng", count);
            
            if (count > 0) {
                return "Thành công: Tìm thấy " + count + " khách hàng. Người đầu tiên: " + response.getMatches(0).getPassengerName();
            }
            return "Thành công: Không có khách hàng nào phù hợp.";
            
        } catch (Exception e) {
            log.error("Lỗi kết nối gRPC: {}", e.getMessage());
            return "Lỗi: " + e.getMessage();
        }
    }
}