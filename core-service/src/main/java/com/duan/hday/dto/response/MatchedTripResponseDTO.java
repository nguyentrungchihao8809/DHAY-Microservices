package com.duan.hday.dto.response;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MatchedTripResponseDTO {
    private Long tripId;
    private String driverName;
    private Double driverRating;
    private String vehicleInfo;
    
    private LocalDateTime departureTime;
    private Integer availableSeats;
    private BigDecimal totalPrice;
    
    // Các chỉ số Matching
    private Integer pickupDistance;  // Đơn vị: mét
    private Integer dropoffDistance; // Đơn vị: mét
    private Double detourDistance;   // Đơn vị: km
}
