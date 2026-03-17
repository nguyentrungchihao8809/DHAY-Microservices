package com.duan.hday.service;

import com.duan.hday.dto.event.NotificationEvent;
import com.duan.hday.entity.Booking;
import com.duan.hday.entity.Trip;
import com.duan.hday.entity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationPublisher notificationPublisher;

    /**
     * HÀM TỔNG: Gửi thông báo dựa trên Enum NotificationType
     * Mọi hàm khác sẽ gọi về đây
     */
    public void sendTypedNotification(Long userId, NotificationType type, Map<String, String> data, Object... args) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(userId)
                .title(type.getTitle())
                .body(type.formatBody(args))
                .data(data != null ? data : new HashMap<>())
                .build();
        
        notificationPublisher.publish(event);
        log.info(">>>> [NOTIFICATION] Sent event {} to User ID: {}", type.name(), userId);
    }

    // --- CÁC HÀM NGHIỆP VỤ CŨ (Đã được Senior hóa) ---

    public void sendBookingRequestToDriver(Long driverId, Booking booking) {
        Map<String, String> data = Map.of(
            "type", "NEW_BOOKING_REQUEST",
            "bookingId", booking.getId().toString(),
            "tripId", booking.getTrip().getId().toString()
        );
        // Gọi enum NEW_BOOKING_REQUEST
        sendTypedNotification(driverId, NotificationType.NEW_BOOKING_REQUEST, data, 
            booking.getPassenger().getFullName(), booking.getSeatsBooked());
    }

    public void sendMatchFoundToPassenger(Long passengerId, Trip trip) {
        Map<String, String> data = Map.of(
            "type", "MATCH_FOUND",
            "tripId", trip.getId().toString()
        );
        // Gọi enum MATCH_FOUND
        sendTypedNotification(passengerId, NotificationType.MATCH_FOUND, data, 
            trip.getDriver().getFullName());
    }

    // Hàm cập nhật trạng thái linh hoạt (cho các trường hợp chưa có trong Enum)
    public void sendTripStatusUpdate(Long userId, String statusName, String message) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(userId)
                .title("Cập nhật chuyến đi")
                .body(message)
                .data(Map.of("type", "TRIP_STATUS", "status", statusName))
                .build();
        notificationPublisher.publish(event);
    }

     public void sendMatchSuggestionToDriver(Long driverId, Long tripId, int count) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(driverId)
                .title("\uD83D\uDCA1 Gợi ý hành khách từ AI")
                .body(String.format("Hệ thống đã tìm thấy %d hành khách tiềm năng dọc lộ trình của bạn.", count))
                .data(Map.of(
                    "type", "MATCH_SUGGESTION",
                    "tripId", tripId.toString(),
                    "count", String.valueOf(count)
                ))
                .build();

        notificationPublisher.publish(event); // Gọi trực tiếp publisher ở đây
        log.info(">>>> [AI-MATCH] Suggested {} passengers to Driver {}", count, driverId);
    }
}