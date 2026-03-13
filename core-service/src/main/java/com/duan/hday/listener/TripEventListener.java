package com.duan.hday.listener;

import com.duan.hday.entity.enums.BookingStatus;
import com.duan.hday.event.TripStatusEvent;
import com.duan.hday.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TripEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleTripStatusChange(TripStatusEvent event) {
        var trip = event.getTrip();
        var status = event.getNewStatus();

        // Kiểm tra an toàn để tránh LazyInitializationException
        if (trip.getBookings() == null) return;

        trip.getBookings().stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .forEach(booking -> {
                String title;
                String body;
                
                switch (status) {
                    case STARTED:
                        title = "🚀 Chuyến đi bắt đầu!";
                        body = "Tài xế đã bắt đầu di chuyển. Hẹn gặp bạn tại điểm đón!";
                        break;
                    case COMPLETED:
                        title = "🏁 Chuyến đi hoàn thành";
                        body = "Cảm ơn bạn đã đồng hành. Đừng quên đánh giá tài xế nhé!";
                        break;
                    case CANCELED:
                        title = "⚠️ Chuyến đi bị hủy";
                        body = "Rất tiếc, tài xế đã hủy chuyến đi này.";
                        break;
                    default: return;
                }

                notificationService.sendNotification(
                    booking.getPassenger().getId(),
                    title,
                    body,
                    Map.of(
                        "tripId", trip.getId().toString(),
                        "status", status.name(),
                        "type", "TRIP_STATUS_UPDATE"
                    )
                );
            });
    }
}