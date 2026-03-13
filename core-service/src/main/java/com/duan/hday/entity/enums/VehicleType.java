package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VehicleType {
    MOTORBIKE("Xe máy"),
    CAR_4_SEATER("Ô tô 4 chỗ"),
    CAR_7_SEATER("Ô tô 7 chỗ");

    private final String label;
}
