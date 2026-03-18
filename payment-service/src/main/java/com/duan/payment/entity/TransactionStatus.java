package com.duan.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionStatus {
    PENDING("Đang chờ thanh toán"),
    SUCCESS("Thanh toán thành công"),
    FAILED("Thanh toán thất bại"),
    EXPIRED("Giao dịch hết hạn"),
    REFUNDED("Đã hoàn tiền");

    private final String label;
}