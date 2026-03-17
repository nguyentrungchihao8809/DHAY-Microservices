package com.duan.hday.service;

import com.duan.hday.entity.*;
import com.duan.hday.entity.enums.*;
import com.duan.hday.exception.AppException;
import com.duan.hday.repository.passenger.BookingRepository;
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;
import com.duan.hday.repository.trip.TripRepository;
import com.duan.hday.util.GeometryUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.LineString;
import lombok.extern.slf4j.Slf4j;
import com.duan.hday.exception.ErrorCode;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final PassengerTripRequestRepository requestRepository;
    private final NotificationService notificationService;
    @Transactional
    public Booking confirmPassenger(Long tripId, Long requestId, User currentUser) {
        // 1. Tìm Trip
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        // DEBUG: Em thêm dòng log này để kiểm tra giá trị thực tế ở console
        log.info("Check Permission - Trip Driver ID: {}, Current User ID: {}", 
                trip.getDriver().getId(), currentUser.getId());

        // 2. Kiểm tra quyền sở hữu
        // Sử dụng Long.valueOf hoặc so sánh trực tiếp nhưng đảm bảo không null
        if (trip.getDriver() == null || !trip.getDriver().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 3. Tìm yêu cầu của khách
        PassengerTripRequest pRequest = requestRepository.findById(requestId)
                .orElse(null);

        if (pRequest == null) {
            // Kiểm tra xem có phải khách này đã có Booking cho Trip này rồi không (trường hợp gọi lần 2)
            return bookingRepository.findByTripIdAndPassengerId(tripId, requestId) // Cần logic check lại
                    .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));
        }

        // Nếu yêu cầu không còn ở trạng thái WAITING
        if (pRequest.getStatus() != RequestStatus.WAITING) {
            // Nếu đã MATCHED với đúng Trip này rồi thì trả về Booking hiện tại luôn (Idempotent)
            if (pRequest.getStatus() == RequestStatus.MATCHED && pRequest.getMatchedTrip().getId().equals(tripId)) {
                return bookingRepository.findByTripAndPassenger(trip, pRequest.getPassenger())
                        .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR));
            }
            throw new AppException(ErrorCode.VALIDATION_ERROR); // Yêu cầu đã bị hủy hoặc khớp với trip khác
        }

        // 4. Kiểm tra số ghế trống
        if (trip.getAvailableSeats() < pRequest.getSeatsRequested()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR); // "Hết chỗ" - Em có thể thêm code TRIP_FULL
        }

        // --- LOGIC XỬ LÝ ---
        Booking booking = Booking.builder()
                .trip(trip)
                .passenger(pRequest.getPassenger())
                .seatsBooked(pRequest.getSeatsRequested())
                .status(BookingStatus.CONFIRMED)
                .build();

        pRequest.setStatus(RequestStatus.MATCHED);
        pRequest.setMatchedTrip(trip);
        trip.setAvailableSeats(trip.getAvailableSeats() - pRequest.getSeatsRequested());

        Booking savedBooking = bookingRepository.save(booking);
        // --- GỬI THÔNG BÁO CHO KHÁCH HÀNG ---
        Map<String, String> data = Map.of(
            "bookingId", savedBooking.getId().toString(),
            "tripId", tripId.toString(),
            "type", "BOOKING_CONFIRMED"
        );

        notificationService.sendTypedNotification(
            savedBooking.getPassenger().getId(), 
            NotificationType.BOOKING_CONFIRMED, 
            data, 
            trip.getDriver().getFullName() // Truyền vào %s trong template
        );

        return savedBooking;
    }

    @Transactional
    public void rejectPassenger(Long requestId, User driver) {
        PassengerTripRequest pRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        pRequest.setStatus(RequestStatus.CANCELED);
        requestRepository.save(pRequest);

        // --- GỬI THÔNG BÁO CHO KHÁCH HÀNG ---
        Map<String, String> data = Map.of(
            "requestId", requestId.toString(),
            "type", "BOOKING_REJECTED"
        );

        notificationService.sendTypedNotification(
            pRequest.getPassenger().getId(), 
            NotificationType.BOOKING_REJECTED, 
            data, 
            driver.getFullName() // Truyền vào %s trong template
        );
    }
    @Transactional
    public void releaseSeats(Booking booking) {
        Trip trip = booking.getTrip();
        if (trip == null) return;

        // 1. Lấy Polyline của chuyến đi để xác định các phân đoạn (Segments)
        LineString routeLine = (LineString) GeometryUtils.wktToGeometry(
                GeometryUtils.castPolylineToWkt(trip.getRoutePolyline())
        );

        // 2. Tìm lại điểm đón/trả của khách trên lộ trình tài xế
        // để tránh phải query ngược lại PassengerTripRequest
        PassengerTripRequest pReq = requestRepository.findByPassengerAndMatchedTrip(booking.getPassenger(), trip)
                .orElse(null);

        if (pReq != null) {
            int startIdx = GeometryUtils.findNearestPointIndex(routeLine, pReq.getStartLocation().getGeom());
            int endIdx = GeometryUtils.findNearestPointIndex(routeLine, pReq.getEndLocation().getGeom());

            // 3. Logic Senior: Cập nhật lại số ghế trống cho Trip
            // Trong mô hình đơn giản: availableSeats là số ghế trống tối thiểu trên toàn hành trình
            // Trong mô hình Segment: Chúng ta cần tính toán lại dựa trên các booking còn lại.
            
            trip.setAvailableSeats(trip.getAvailableSeats() + booking.getSeatsBooked());
            
            // Nếu trước đó Trip bị FULL, giờ có chỗ thì mở lại status OPEN
            if (trip.getStatus() == TripStatus.FULL) {
                trip.setStatus(TripStatus.OPEN);
            }
            
            tripRepository.save(trip);
            log.info("Released {} seats for Trip {} from segment {} to {}", 
                    booking.getSeatsBooked(), trip.getId(), startIdx, endIdx);
        }
    }
}
