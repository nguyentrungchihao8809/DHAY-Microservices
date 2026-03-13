package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BookingStatus {

    /**
     * Booking vừa được tạo
     * - Chưa giữ ghế chính thức
     * - Chờ thanh toán
     */
    PENDING("Đang chờ xác nhận"),

    /**
     * Thanh toán thành công
     * - Ghế đã được giữ
     * - Driver & Passenger đã ràng buộc
     */
    CONFIRMED("Đã xác nhận"),

    /**
     * Passenger hoặc hệ thống hủy
     * - Thanh toán thất bại
     * - Trip bị hủy
     */
    CANCELED("Đã hủy"),

    /**
     * Chuyến đi đã hoàn thành
     * - Cho phép review
     */
    COMPLETED("Đã hoàn thành");
    
    private final String label;
}
