package com.duan.payment.entity;

import com.duan.payment.common.BaseEntity;
import com.duan.payment.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID idempotencyKey; // Dùng để check trùng lặp

    @Column(nullable = false)
    private Long passengerId;

    @Column(nullable = false)
    private Long tripId;

    @Column(nullable = false)
    private Long requestId; // ID từ PassengerTripRequest bên Core

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String paymentMethod; // VNPAY, MOMO, ZALOPAY...

    @Column(columnDefinition = "TEXT")
    private String description;
}