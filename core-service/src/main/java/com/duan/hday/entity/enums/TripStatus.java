package com.duan.hday.entity.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TripStatus {
    OPEN("Đang tìm khách"),
    FULL("Đã đủ chỗ"),
    STARTED("Chuyến đi bắt đầu"),
    COMPLETED("Đã hoàn thành"),
    CANCELED("Đã hủy");

    private final String label;
}
