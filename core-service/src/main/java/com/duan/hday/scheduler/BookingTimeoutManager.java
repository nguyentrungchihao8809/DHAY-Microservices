package com.duan.hday.scheduler;

import com.duan.hday.entity.Booking;
import com.duan.hday.entity.enums.BookingStatus;
import com.duan.hday.entity.enums.RequestStatus;
import com.duan.hday.event.BookingEvent;
import com.duan.hday.repository.passenger.BookingRepository;
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;
import com.duan.hday.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingTimeoutManager {

    private final BookingRepository bookingRepository;
    private final PassengerTripRequestRepository requestRepository;
    private final BookingService bookingService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 300000) // 5 phút quét 1 lần
    @Transactional
    public void handlePendingBookingTimeouts() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndCreatedAtBefore(BookingStatus.PENDING, threshold);

        if (expiredBookings.isEmpty()) return;

        log.info(">>>> Found {} expired pending bookings. Processing...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                // 1. Cập nhật trạng thái Booking
                booking.setStatus(BookingStatus.CANCELED);
                
                if (booking.getTrip() != null) {
                    // 2. Hoàn ghế cho Trip
                    bookingService.releaseSeats(booking); 

                    // 3. Reset Request của khách về WAITING để hệ thống ghép xe lại
                    requestRepository.findByPassengerAndMatchedTrip(booking.getPassenger(), booking.getTrip())
                        .ifPresent(req -> {
                            req.setStatus(RequestStatus.WAITING);
                            req.setMatchedTrip(null);
                            requestRepository.save(req);
                        });
                }

                bookingRepository.save(booking);

                // 4. PHÁT EVENT (Mọi việc gửi notification sẽ do Listener xử lý)
                eventPublisher.publishEvent(new BookingEvent(booking, "TIMEOUT"));
                
                log.info(">>>> Booking ID {} timed out and released.", booking.getId());
                
            } catch (Exception e) {
                log.error(">>>> Error processing timeout for booking ID {}: {}", booking.getId(), e.getMessage());
            }
        }
    }
}