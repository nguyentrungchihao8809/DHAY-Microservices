package com.duan.hday.repository.passenger;
import com.duan.hday.entity.Booking;
import com.duan.hday.entity.Trip;
import com.duan.hday.entity.enums.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.duan.hday.entity.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Thêm dòng này để tìm Booking dựa trên Trip và Passenger
    Optional<Booking> findByTripAndPassenger(Trip trip, User passenger);

    // Tiện tay thêm luôn phương thức này nếu em cần dùng ID cho gọn
    Optional<Booking> findByTripIdAndPassengerId(Long tripId, Long passengerId);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime dateTime);
}
