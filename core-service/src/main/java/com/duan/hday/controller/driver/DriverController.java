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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final TripService tripService;
    private final BookingService bookingService;

    // Giả định Header tên là "X-User-Id" được Gateway đẩy xuống sau khi verify JWT
    private static final String USER_ID_HEADER = "X-User-Id";

    // --- QUẢN LÝ HỒ SƠ TÀI XẾ ---

    @PostMapping("/register")
    public ResponseEntity<?> registerDriver(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody @Valid DriverRegistrationRequest request
    ) {
        driverService.registerAsDriver(userId, request);
        return ResponseEntity.ok(Map.of(
            "message", "Đăng ký tài xế thành công! Vui lòng chờ phê duyệt."
        ));
    }

    @GetMapping("/check-registration")
    public ResponseEntity<?> checkRegistration(@RequestHeader(USER_ID_HEADER) Long userId) {
        boolean isRegistered = driverService.isDriver(userId);
        return ResponseEntity.ok(Map.of(
            "isRegistered", isRegistered,
            "message", isRegistered ? "Đã đăng ký tài xế" : "Chưa đăng ký tài xế"
        ));
    }

    // --- QUẢN LÝ CHUYẾN ĐI (TRIPS) ---

    @PostMapping("/trips")
    public ResponseEntity<?> createTrip(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody TripCreateDTO dto
    ) {
        Trip trip = tripService.createTrip(dto, userId);
        return ResponseEntity.ok(Map.of(
            "message", "Bạn đã tạo chuyến xe thành công!",
            "tripId", trip.getId()
        ));
    }

    @GetMapping("/trips/recent")
    public ResponseEntity<List<RecentTripResponseDTO>> getRecentTrips(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return ResponseEntity.ok(tripService.getRecentTrips(userId));
    }

    @PatchMapping("/trips/{tripId}/start")
    public ResponseEntity<?> startTrip(
            @PathVariable Long tripId,
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        tripService.updateTripStatus(tripId, TripStatus.STARTED, userId);
        return ResponseEntity.ok(Map.of("message", "Chuyến đi đã bắt đầu. Chúc bạn thượng lộ bình an!"));
    }

    @PatchMapping("/trips/{tripId}/complete")
    public ResponseEntity<?> completeTrip(
            @PathVariable Long tripId,
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        tripService.updateTripStatus(tripId, TripStatus.COMPLETED, userId);
        return ResponseEntity.ok(Map.of("message", "Chúc mừng! Bạn đã hoàn thành chuyến đi."));
    }

    @PatchMapping("/trips/{tripId}/cancel")
    public ResponseEntity<?> cancelTrip(
            @PathVariable Long tripId,
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        tripService.updateTripStatus(tripId, TripStatus.CANCELED, userId);
        return ResponseEntity.ok(Map.of("message", "Chuyến đi đã được hủy."));
    }

    // --- QUẢN LÝ HÀNH KHÁCH (BOOKINGS) ---

    @PostMapping("/trips/confirm-passenger")
    public ResponseEntity<?> confirmPassenger(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody ConfirmBookingRequest request
    ) {
        Booking booking = bookingService.confirmPassenger(
            request.getTripId(), 
            request.getPassengerRequestId(), 
            userId
        );

        return ResponseEntity.ok(Map.of(
            "message", "Xác nhận hành khách thành công!",
            "bookingId", booking.getId()
        ));
    }

    @PostMapping("/trips/reject-passenger/{requestId}")
    public ResponseEntity<?> rejectPassenger(
            @PathVariable Long requestId,
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        bookingService.rejectPassenger(requestId, userId);
        return ResponseEntity.ok(Map.of("message", "Đã từ chối yêu cầu của khách."));
    }
}