package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingStatus {
    AWAITING_PAYMENT, // Khách vừa bấm đặt, chưa thanh toán (nếu chọn VNPay/Momo)
    PAID,             // Đã thanh toán xong, đang đợi hệ thống tìm tài xế
    CONFIRMED,        // Đã có tài xế nhận chuyến
    ON_THE_WAY,       // Tài xế đang đón khách
    IN_PROGRESS,      // Đang di chuyển
    COMPLETED,        // Hoàn thành
    CANCELLED         // Đã hủy
}
