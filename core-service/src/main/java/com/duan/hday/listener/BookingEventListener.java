package com.duan.hday.listener;

import com.duan.hday.event.BookingEvent;
import com.duan.hday.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import com.duan.hday.entity.Booking;

@Component
@RequiredArgsConstructor
public class BookingEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleBookingEvent(BookingEvent event) {
        Booking booking = event.getBooking();
        String type = event.getEventType();
        Long passengerId = booking.getPassenger().getId();
        
        String title;
        String body;
        Map<String, String> data = new HashMap<>();
        data.put("type", "BOOKING_" + type);

        switch (type) {
            case "CONFIRMED":
                title = "✅ Chuyến xe đã được xác nhận!";
                body = String.format("Tài xế %s đã chấp nhận yêu cầu của bạn.", 
                        booking.getTrip().getDriver().getFullName());
                data.put("bookingId", booking.getId().toString());
                data.put("tripId", booking.getTrip().getId().toString());
                break;

            case "REJECTED":
                title = "❌ Yêu cầu bị từ chối";
                body = "Rất tiếc, tài xế không thể thực hiện chuyến đi này cùng bạn.";
                break;

            case "TIMEOUT":
                title = "⏳ Yêu cầu hết hạn";
                body = "Tài xế không phản hồi, hệ thống đang tìm chuyến xe khác cho bạn.";
                break;

            default:
                return;
        }

        notificationService.sendNotification(passengerId, title, body, data);
    }
}