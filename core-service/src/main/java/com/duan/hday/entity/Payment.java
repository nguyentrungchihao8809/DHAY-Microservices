package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import com.duan.hday.entity.enums.PaymentMethod;
import com.duan.hday.entity.enums.PaymentStatus;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;
}
