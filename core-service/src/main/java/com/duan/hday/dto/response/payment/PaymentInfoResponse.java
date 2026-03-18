//DTO dùng để trả về thông tin giá tiền và trạng thái chuyến đi cho Payment-service.
package com.duan.hday.dto.response.payment;


import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoResponse {
    private Long bookingId;
    private Long tripId;
    private BigDecimal amount;
    private String currency; // "VND"
    private String status;   // BookingStatus hiện tại
    private String description; // Nội dung thanh toán (vd: "Thanh toán chuyến đi HN-HP")
}
