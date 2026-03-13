package com.duan.hday.dto.response;

import com.duan.hday.entity.enums.TripStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TripDetailResponseDTO {

    private Long tripId; 
    private TripStatus status; 

    // Thông tin địa điểm
    private String startAddress; 
    private String endAddress; 

    // Thông tin thời gian
    private LocalDateTime departureTime;
    private LocalDateTime createdAt;

    // Thông tin phương tiện
    private VehicleSummaryDTO vehicleInfo;

    // Thông tin ghế ngồi và giá
    private Integer totalSeats; 
    private Integer availableSeats;
    private Integer passengerCount; // Số khách thực tế đã đặt (total - available)
    private BigDecimal pricePerSeat; 

    // Lộ trình vẽ bản đồ
    private String routePolyline;

    // Ghi chú thêm
    private String note;

    private String message; // Thông báo trạng thái bổ sung
}
