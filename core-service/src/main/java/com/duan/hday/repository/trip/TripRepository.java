package com.duan.hday.repository.trip;

import com.duan.hday.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType; // Nếu dùng Lock
import java.util.Optional;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    // 1. Hàm này dùng để lấy Trip kèm theo StartLocation và EndLocation (tránh lỗi LazyInitializationException)
    @Query("SELECT t FROM Trip t " +
           "JOIN FETCH t.startLocation " +
           "JOIN FETCH t.endLocation " + 
           "WHERE t.id = :id")
    Optional<Trip> findTripWithLocations(@Param("id") Long id);

    // 2. Hàm check trùng lịch có Lock (đã fix ở bước trước)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(t) > 0 FROM Trip t " +
           "WHERE t.driver.id = :driverId " +
           "AND t.id <> :currentTripId " +
           "AND t.status <> 'CANCELLED' " + 
           "AND :newStartTime < t.estimatedArrivalTime " +
           "AND :newEndTime > t.departureTime")
    boolean existsOverlappingTripWithLock(
            @Param("driverId") Long driverId,
            @Param("currentTripId") Long currentTripId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime
    );

    @Query("SELECT t FROM Trip t WHERE t.driver.id = :driverId ORDER BY t.id DESC")
    List<Trip> findTop4ByDriverIdOrderByCreatedAt(@Param("driverId") Long driverId, org.springframework.data.domain.Pageable pageable);
}