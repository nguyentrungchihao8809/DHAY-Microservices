package com.duan.hday.controller.driver;


import com.duan.hday.dto.request.driver.ConfirmBookingRequest;
import com.duan.hday.dto.request.driver.DriverRegistrationRequest;
import com.duan.hday.dto.request.driver.TripCreateDTO;
import com.duan.hday.dto.response.RecentTripResponseDTO;
import com.duan.hday.entity.Booking;
import com.duan.hday.entity.Trip;
import com.duan.hday.entity.enums.TripStatus;
import com.duan.hday.service.BookingService;
import com.duan.hday.service.DriverService;
import com.duan.hday.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.duan.hday.config.UserPrincipal;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final TripService tripService;
    private final BookingService bookingService;

    @PostMapping("/register")
    public ResponseEntity<String> registerDriver(
                @AuthenticationPrincipal UserPrincipal principal, // Lấy trực tiếp Principal đã đồng nhất
                @RequestBody @Valid DriverRegistrationRequest request
            ) {
                // 1. Lấy userId cực kỳ đơn giản và an toàn
                Long currentUserId = principal.getUserId(); 
                
                // 2. Thực thi nghiệp vụ
                driverService.registerAsDriver(currentUserId, request);
                
                return ResponseEntity.ok("Đăng ký trở thành tài xế thành công! Vui lòng chờ phê duyệt.");
                //cần trả về idVihecle để FE lưu tạm
            }

    // API phục vụ việc check xem user "là ai" để FE điều hướng
    @GetMapping("/check-registration")
    public ResponseEntity<?> checkRegistration(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isRegistered = driverService.isDriver(principal.getUserId());
        
        return ResponseEntity.ok(Map.of(
            "isRegistered", isRegistered,
            "message", isRegistered ? "Đã đăng ký tài xế" : "Chưa đăng ký tài xế"
        ));
    }

    @PostMapping("/trips")
    public ResponseEntity<?> createTrip(
        @Valid @RequestBody TripCreateDTO dto, 
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 1. Thực thi nghiệp vụ tạo chuyến
        Trip trip = tripService.createTrip(dto, principal.getUser());

        // 2. Trả về thông báo thành công kèm ID để FE điều hướng nếu cần
        return ResponseEntity.ok(Map.of(
            "message", "Bạn đã tạo chuyến xe thành công!",
            "tripId", trip.getId()
        ));
    }

    @PostMapping("/trips/confirm-passenger")
    public ResponseEntity<?> confirmPassenger(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ConfirmBookingRequest request
    ) {
        Booking booking = bookingService.confirmPassenger(
            request.getTripId(), 
            request.getPassengerRequestId(), 
            principal.getUser()
        );

        return ResponseEntity.ok(Map.of(
            "message", "Xác nhận hành khách thành công!",
            "bookingId", booking.getId()
        ));
    }

    /**
     * API 1: Bắt đầu chuyến đi
     */
    @PatchMapping("/trips/{tripId}/start")
        public ResponseEntity<?> startTrip(
                @PathVariable Long tripId,
                @AuthenticationPrincipal UserPrincipal principal
        ) {
            tripService.startTrip(tripId, principal.getUser());
            return ResponseEntity.ok(Map.of("message", "Chuyến đi đã bắt đầu. Chúc bạn thượng lộ bình an!"));
        }

        /**
         * API 2: Hoàn thành chuyến đi
         */
    @PatchMapping("/trips/{tripId}/complete")
        public ResponseEntity<?> completeTrip(
                @PathVariable Long tripId,
                @AuthenticationPrincipal UserPrincipal principal
        ) {
            tripService.completeTrip(tripId, principal.getUser());
            return ResponseEntity.ok(Map.of("message", "Chúc mừng! Bạn đã hoàn thành chuyến đi."));
        }

        /**
         * API 3: Hủy chuyến đi (Trường hợp khẩn cấp)
         */
    @PatchMapping("/trips/{tripId}/cancel")
        public ResponseEntity<?> cancelTrip(
                @PathVariable Long tripId,
                @AuthenticationPrincipal UserPrincipal principal
        ) {
            // Gọi thẳng hàm update chung với status CANCELED
            tripService.updateTripStatus(tripId, TripStatus.CANCELED, principal.getUser());
            return ResponseEntity.ok(Map.of("message", "Chuyến đi đã được hủy thành công."));
        }

    @PutMapping("/trips/{tripId}")
    public ResponseEntity<?> updateTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody TripCreateDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Trip updatedTrip = tripService.updateTrip(tripId, dto, principal.getUser());
        
        return ResponseEntity.ok(Map.of(
            "message", "Cập nhật chuyến xe thành công!",
            "tripId", updatedTrip.getId(),
            "status", updatedTrip.getStatus()
        ));
    }

    @GetMapping("/trips/recent")
    public ResponseEntity<List<RecentTripResponseDTO>> getRecentTrips(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<RecentTripResponseDTO> recentTrips = tripService.getRecentTrips(principal.getUser());
        return ResponseEntity.ok(recentTrips);
}
}
