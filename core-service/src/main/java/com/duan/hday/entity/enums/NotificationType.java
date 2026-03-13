package com.duan.hday.entity.enums;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum NotificationType {
    NEW_BOOKING_REQUEST,    // Có khách đặt chỗ (Gửi cho tài xế)
    BOOKING_CONFIRMED,      // Tài xế đã xác nhận (Gửi cho khách)
    MATCH_FOUND,           // Hệ thống tìm thấy chuyến phù hợp (Gửi cho khách)
    TRIP_STARTED           // Chuyến đi bắt đầu (Gửi cho khách)
}
