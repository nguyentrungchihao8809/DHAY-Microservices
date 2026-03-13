package com.duan.hday.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {

    /**
     * Trả tiền mặt cho driver
     * - Thanh toán sau khi hoàn thành chuyến
     * - Không có callback từ cổng
     */
    CASH("Tiền mặt"),

    /**
     * Ví nội bộ của hệ thống
     * - Có số dư
     * - Có thể hoàn tiền tức thì
     */
    WALLET("Ví nội bộ"),

    /**
     * Ví điện tử MOMO
     */
    MOMO("MOMO"),

    /**
     * ZaloPay
     */
    ZALOPAY("ZaloPay"),

    /**
     * VNPay
     */
    VNPAY("VNPay");

    private final String label;
}
