package com.duan.payment.entity;

import com.duan.payment.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_transactions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_request_id")
    private PaymentRequest paymentRequest;

    private String externalTransactionId; // Mã GD của VNPAY/Momo

    @Column(columnDefinition = "TEXT")
    private String rawResponseLog; // Lưu JSON/Query thô từ Gateway

    private String responseCode; // Ví dụ: 00 là thành công của VNPAY
}