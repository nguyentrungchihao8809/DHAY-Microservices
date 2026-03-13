package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {

    /**
     * Đã tạo payment nhưng CHƯA thanh toán
     * - Chưa gọi cổng
     * - Hoặc đang chờ user xác nhận
     */
    PENDING("Đang chờ thanh toán"),

    /**
     * Đang xử lý với cổng thanh toán
     * - Đã redirect sang MOMO / ZaloPay / VNPay
     * - Chưa có kết quả cuối
     */
    PROCESSING("Đang xử lý"),

    /**
     * Thanh toán thành công
     * - Tiền đã ghi nhận
     * - Booking có thể CONFIRMED
     */
    SUCCESS("Thanh toán thành công"),

    /**
     * Thanh toán thất bại
     * - User hủy
     * - Cổng trả lỗi
     */
    FAILED("Thanh toán thất bại"),

    /**
     * Đã hoàn tiền
     * - Trip bị hủy
     * - Passenger hủy đúng chính sách
     */
    REFUNDED("Đã hoàn tiền");

    private final String label;
}
