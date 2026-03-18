package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestStatus {
    WAITING( "Đang chờ"),
    PAID("Đã thanh toán"),
    MATCHED( "Đã ghép"),
    EXPIRED( "Hết hạn"),
    CANCELED( "Đã hủy");

     private final String label;
}
