package com.duan.hday.controller.passenger;

import com.duan.hday.dto.request.passenger.LocationRequestDTO;
import com.duan.hday.dto.request.passenger.PassengerTripRequestDTO;
import com.duan.hday.dto.response.passenger.PriceEstimationResponse;
import com.duan.hday.service.PassengerTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerTripService passengerTripService;

    // Tên Header thống nhất toàn hệ thống
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * BƯỚC 1: Ước tính giá dựa trên tọa độ (Không cần Auth nếu muốn)
     */
    @PostMapping("/estimate")
    public ResponseEntity<PriceEstimationResponse> getEstimation(
            @RequestBody LocationRequestDTO locationDto) {
        
        PriceEstimationResponse response = passengerTripService.estimateTripPrice(
                locationDto.getStartLat(), 
                locationDto.getStartLng(), 
                locationDto.getEndLat(), 
                locationDto.getEndLng()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * BƯỚC 2: Đặt xe chính thức (Gửi request lên hệ thống)
     */
    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(
            @RequestBody PassengerTripRequestDTO dto, 
            @RequestHeader(USER_ID_HEADER) Long passengerId) {
        
        // Thực thi nghiệp vụ đặt xe
        passengerTripService.processTripRequest(dto, passengerId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Yêu cầu đặt xe đã được ghi nhận! Đang tìm tài xế phù hợp..."
        ));
    }

    /**
     * Hủy yêu cầu đặt xe
     */
    @PatchMapping("/requests/{id}/cancel")
    public ResponseEntity<?> cancelRequest(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) Long passengerId
    ) {
        passengerTripService.cancelTripRequest(id, passengerId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Yêu cầu chuyến đi đã được hủy thành công."
        ));
    }

    /**
     * Cập nhật yêu cầu (Thay đổi điểm đón/trả/số ghế khi đang chờ)
     */
    @PutMapping("/requests/{id}")
    public ResponseEntity<?> updateRequest(
            @PathVariable Long id,
            @RequestBody PassengerTripRequestDTO dto,
            @RequestHeader(USER_ID_HEADER) Long passengerId
    ) {
        passengerTripService.updateTripRequest(id, dto, passengerId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Cập nhật yêu cầu thành công!"
        ));
    }
}