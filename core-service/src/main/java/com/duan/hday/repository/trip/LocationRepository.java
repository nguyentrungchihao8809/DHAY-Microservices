package com.duan.hday.repository.trip;

import com.duan.hday.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = """
        SELECT COUNT(pr.id) 
        FROM passenger_trip_requests pr
        JOIN locations l ON pr.start_location_id = l.id
        WHERE pr.status = 'WAITING'
        -- Ép kiểu tham số truyền vào thành timestamp để Postgres không bị bối rối
        AND pr.desired_departure_time BETWEEN CAST(:startTime AS TIMESTAMP) AND CAST(:endTime AS TIMESTAMP)
        AND ST_DWithin(
            l.geom::geography, 
            ST_GeomFromText(:wktLine, 4326)::geography, 
            500
        )
        """, nativeQuery = true)
    int countPotentialPassengersAlongRoute(
            @Param("wktLine") String wktLine, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime
    );
}