package com.duan.hday.service;

import com.duan.hday.entity.*;
import com.duan.hday.entity.enums.*;
import com.duan.hday.exception.AppException;
import com.duan.hday.exception.ErrorCode;
import com.duan.hday.repository.auth.UserRepository;
import com.duan.hday.repository.passenger.BookingRepository;
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;
import com.duan.hday.repository.trip.TripRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final PassengerTripRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Tài xế chấp nhận yêu cầu của khách
     * @param tripId: ID chuyến đi của tài xế
     * @param requestId: ID yêu cầu của khách
     * @param driverId: ID tài xế thực hiện thao tác (lấy từ Gateway)
     */
    @Transactional
    public Booking confirmPassenger(Long tripId, Long requestId, Long driverId) {
        // 1. Tìm Trip và kiểm tra quyền sở hữu
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 2. Tìm yêu cầu của khách
        PassengerTripRequest pRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));

        // 3. Xử lý Idempotent (Nếu khách đã được confirm vào trip này rồi thì trả về luôn)
        if (pRequest.getStatus() == RequestStatus.MATCHED) {
            if (pRequest.getMatchedTrip().getId().equals(tripId)) {
                return bookingRepository.findByTripIdAndPassengerId(tripId, pRequest.getPassenger().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));
            }
            throw new AppException(ErrorCode.VALIDATION_ERROR); // Khách đã thuộc về trip khác
        }

        // 4. Kiểm tra trạng thái và số ghế
        if (pRequest.getStatus() != RequestStatus.WAITING) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        if (trip.getAvailableSeats() < pRequest.getSeatsRequested()) {
            throw new RuntimeException("Chuyến đi không còn đủ ghế trống!");
        }

        // 5. Tạo Booking & Cập nhật trạng thái
        Booking booking = Booking.builder()
                .trip(trip)
                .passenger(pRequest.getPassenger())
                .seatsBooked(pRequest.getSeatsRequested())
                .status(BookingStatus.CONFIRMED)
                .build();

        pRequest.setStatus(RequestStatus.MATCHED);
        pRequest.setMatchedTrip(trip);
        
        // Cập nhật số ghế trống
        trip.setAvailableSeats(trip.getAvailableSeats() - pRequest.getSeatsRequested());
        if (trip.getAvailableSeats() == 0) {
            trip.setStatus(TripStatus.FULL);
        }

        Booking savedBooking = bookingRepository.save(booking);
        tripRepository.save(trip); // Lưu lại trạng thái Trip

        // 6. Gửi thông báo cho khách hàng
        notifyPassenger(savedBooking, trip, NotificationType.BOOKING_CONFIRMED);

        return savedBooking;
    }

    /**
     * Tài xế từ chối/hủy yêu cầu của khách
     */
    @Transactional
    public void rejectPassenger(Long requestId, Long driverId) {
        PassengerTripRequest pRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request không tồn tại"));

        // Logic check driverId ở đây có thể cần thông qua TripId nếu request đã matched
        // Hoặc đơn giản là tài xế bấm "Bỏ qua" trên danh sách gợi ý.
        
        pRequest.setStatus(RequestStatus.CANCELED);
        requestRepository.save(pRequest);

        // Lấy Proxy Driver để lấy thông tin tên gửi thông báo
        User driverProxy = userRepository.findById(driverId).orElse(null);
        String driverName = (driverProxy != null) ? driverProxy.getFullName() : "Tài xế";

        notificationService.sendTypedNotification(
            pRequest.getPassenger().getId(), 
            NotificationType.BOOKING_REJECTED, 
            Map.of("requestId", requestId.toString(), "type", "BOOKING_REJECTED"), 
            driverName
        );
    }

    /**
     * Hoàn lại ghế khi booking bị hủy hoặc kết thúc
     */
    @Transactional
    public void releaseSeats(Booking booking) {
        Trip trip = booking.getTrip();
        if (trip == null) return;

        // Hoàn ghế
        trip.setAvailableSeats(trip.getAvailableSeats() + booking.getSeatsBooked());
        
        // Mở lại trạng thái nếu đang Full
        if (trip.getStatus() == TripStatus.FULL) {
            trip.setStatus(TripStatus.OPEN);
        }
        
        tripRepository.save(trip);
        log.info("Released {} seats for Trip ID {}", booking.getSeatsBooked(), trip.getId());
    }

    private void notifyPassenger(Booking booking, Trip trip, NotificationType type) {
        try {
            Map<String, String> data = Map.of(
                "bookingId", booking.getId().toString(),
                "tripId", trip.getId().toString(),
                "type", type.name()
            );

            notificationService.sendTypedNotification(
                booking.getPassenger().getId(), 
                type, 
                data, 
                trip.getDriver().getFullName()
            );
        } catch (Exception e) {
            log.error("Lỗi gửi thông báo Booking: {}", e.getMessage());
        }
    }
}