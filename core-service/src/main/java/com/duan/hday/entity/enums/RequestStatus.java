package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestStatus {
    WAITING( "Đang chờ"),
    MATCHED( "Đã ghép"),
    EXPIRED( "Hết hạn"),
    CANCELED( "Đã hủy");

     private final String label;
}
