package com.duan.payment.repository;

import com.duan.payment.entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    Optional<PaymentRequest> findByIdempotencyKey(UUID idempotencyKey);
    Optional<PaymentRequest> findByRequestId(Long requestId);
}