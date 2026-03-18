package com.duan.payment.repository;

import com.duan.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    // Thêm dòng này để hết lỗi undefined
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);
}