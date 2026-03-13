package com.duan.hday.repository.passenger;

import com.duan.hday.entity.PassengerTripRequest;
import com.duan.hday.entity.Trip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.duan.hday.entity.User;
import com.duan.hday.entity.enums.RequestStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PassengerTripRequestRepository extends JpaRepository<PassengerTripRequest, Long> {

     @Query(value = """
        SELECT pr.* FROM passenger_trip_requests pr
        JOIN locations ls ON pr.start_location_id = ls.id
        JOIN locations le ON pr.end_location_id = le.id
        WHERE pr.status = 'WAITING'
          AND pr.desired_departure_time BETWEEN :windowStart AND :windowEnd
          -- Điểm đón (start) nằm trong phạm vi 1km (1000m) quanh tuyến đường
          AND ST_DWithin(
            ls.geom::geography, 
            ST_GeomFromText(:polylineWkt, 4326)::geography, 
            1000
          )
          -- Điểm trả (end) nằm trong phạm vi 2.5km (2500m) quanh tuyến đường
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
    Optional<PassengerTripRequest> findByPassengerAndMatchedTrip(User passenger, Trip trip);

    boolean existsByPassengerAndStatusAndCreatedAtAfter(
            User passenger, 
            RequestStatus status, 
            LocalDateTime dateTime
    );
}
