package com.duan.hday.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PassengerTripDetailDTO {
    private Long requestId;
    private String status;
    
    private String startAddress;
    private String endAddress;
    
    private LocalDateTime departureTime;
    private Integer numberOfSeats;
    
    private BigDecimal totalPrice;
    private String paymentStatus;
    private String note;
    
    // Đối tượng lồng nhau để chứa thông tin tài xế
    private DriverSummaryDTO driverInfo;

    @Getter @Setter @Builder
    public static class DriverSummaryDTO {
        private String driverName;
        private String vehicleName;
        private String vehiclePlate;
        private Double rating;
    }
}