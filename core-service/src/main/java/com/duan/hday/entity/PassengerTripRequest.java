package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.duan.hday.entity.enums.RequestStatus;
import com.duan.hday.entity.enums.VehicleType;

@Entity
@Table(name = "passenger_trip_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PassengerTripRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    private LocalDateTime desiredDepartureTime;

    @Builder.Default
    private Integer seatsRequested = 1;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @ManyToOne
    @JoinColumn(name = "matched_trip_id")
    private Trip matchedTrip;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private BigDecimal estimatedPrice;

    private Double distanceKm;
}
