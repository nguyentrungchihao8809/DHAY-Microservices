package com.duan.hday.dto.request.passenger;


import lombok.*;
import java.math.BigDecimal;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BookingRequestDTO {
    private Long tripId;
    private Long requestId;
    private Integer numberOfSeats;
    private BigDecimal totalPrice;
    private String note;
}