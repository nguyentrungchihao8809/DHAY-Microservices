package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import com.duan.hday.entity.enums.BookingStatus;

@Entity
@Table(name = "bookings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    private Integer seatsBooked;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
