package com.duan.hday.repository.passenger;

import com.duan.hday.entity.PassengerTripRequest;
import com.duan.hday.entity.Trip;
import com.duan.hday.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PassengerTripRequestRepository extends JpaRepository<PassengerTripRequest, Long> {

    @Query(value = """
        SELECT pr.* FROM passenger_trip_requests pr
        JOIN locations ls ON pr.start_location_id = ls.id
        JOIN locations le ON pr.end_location_id = le.id
        WHERE pr.status = 'WAITING'
          AND pr.desired_departure_time BETWEEN :windowStart AND :windowEnd
          AND ST_DWithin(
            ls.geom::geography, 
            ST_GeomFromText(:polylineWkt, 4326)::geography, 
            1000
          )
          AND ST_DWithin(
            le.geom::geography, 
            ST_GeomFromText(:polylineWkt, 4326)::geography, 
            2500
          )
        """, nativeQuery = true)
    List<PassengerTripRequest> findAllPotentialMatches(
            @Param("polylineWkt") String polylineWkt,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );

    // Sửa: Tìm theo ID của Passenger
    Optional<PassengerTripRequest> findByPassengerIdAndMatchedTripId(Long passengerId, Long tripId);

    // Sửa quan trọng: Kiểm tra chống spam dùng ID
    boolean existsByPassengerIdAndStatusAndCreatedAtAfter(
            Long passengerId, 
            RequestStatus status, 
            LocalDateTime dateTime
    );
}