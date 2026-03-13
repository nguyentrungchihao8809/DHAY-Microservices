package com.duan.hday.entity.enums;

import lombok.Getter;

@Getter
public enum ReviewType {
    PASSENGER_TO_DRIVER("Hành khách đánh giá tài xế"),
    DRIVER_TO_PASSENGER("Tài xế đánh giá hành khách");

    private final String label;
    ReviewType(String label) { this.label = label; }
}
