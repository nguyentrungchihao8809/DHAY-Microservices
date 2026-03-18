package com.duan.payment.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
public class CoreTripResponse {
    private Long bookingId;
    private Long tripId;
    private BigDecimal amount;
    private String currency;
    private String description;
}
