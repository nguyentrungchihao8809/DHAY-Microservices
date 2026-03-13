package com.duan.hday.controller.passenger;

import com.duan.hday.dto.request.passenger.LocationRequestDTO;
import com.duan.hday.dto.request.passenger.PassengerTripRequestDTO;
import com.duan.hday.dto.response.passenger.PriceEstimationResponse;
import com.duan.hday.service.PassengerTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.duan.hday.config.UserPrincipal;


@RestController
@RequestMapping("/api/v1/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerTripService passengerTripService;
    private final PassengerTripService tripService;

    @PostMapping("/estimate")
    public ResponseEntity<PriceEstimationResponse> getEstimation(
            @RequestBody LocationRequestDTO locationDto) {
        
        PriceEstimationResponse response = tripService.estimateTripPrice(
            locationDto.getStartLat(), 
            locationDto.getStartLng(), 
            locationDto.getEndLat(), 
            locationDto.getEndLng()
        );
        return ResponseEntity.ok(response);
    }

    /**
 * BƯỚC 2: Đặt xe chính thức
 */
    @PostMapping("/requests")
    public ResponseEntity<String> createRequest(
            @RequestBody PassengerTripRequestDTO dto, 
            @AuthenticationPrincipal UserPrincipal principal) { // Đổi User thành UserPrincipal
        
        // Kiểm tra an toàn
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(401).body("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại!");
        }

        // Truyền principal.getUser() vào service
        tripService.processTripRequest(dto, principal.getUser());
        
        return ResponseEntity.ok("Yêu cầu đặt xe đã được ghi nhận!");
    }

    @PatchMapping("/requests/{id}/cancel")
    public ResponseEntity<String> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        passengerTripService.cancelTripRequest(id, principal.getUser());
        return ResponseEntity.ok("Yêu cầu chuyến đi đã được hủy thành công.");
    }

    @GetMapping("/requests/estimate-price")
    public ResponseEntity<PriceEstimationResponse> estimatePrice(
            @RequestParam Double sLat, @RequestParam Double sLng,
            @RequestParam Double eLat, @RequestParam Double eLng
    ) {
        return ResponseEntity.ok(passengerTripService.estimateTripPrice(sLat, sLng, eLat, eLng));
    }
}