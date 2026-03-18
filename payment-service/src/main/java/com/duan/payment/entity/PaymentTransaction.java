package com.duan.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private String transactionRef; // Mã đơn hàng gửi sang VNPay/Momo
    private String provider;       // MOMO, VNPAY
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // PENDING, SUCCESS, FAILED

    private String providerTransactionId; // Mã giao dịch của cổng trả về
    private LocalDateTime createdAt;
}