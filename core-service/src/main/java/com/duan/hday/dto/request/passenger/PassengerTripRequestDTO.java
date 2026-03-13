package com.duan.hday.dto.request.passenger;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.duan.hday.entity.enums.VehicleType;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PassengerTripRequestDTO {

    // Điểm đi (FR-P-06)
    private String startAddress;
    private Double startLat;
    private Double startLng;

    // Điểm đến (FR-P-07)
    private String endAddress;
    private Double endLat;
    private Double endLng;

    /**
     * Thời gian khởi hành (FR-P-08)
     * Nếu đi ngay (FR-P-04), gửi thời gian hiện tại.
     * Nếu hẹn trước (FR-P-05), gửi thời gian mong muốn.
     */
    private LocalDateTime departureTime;

    private Integer numberOfSeats; // Số ghế cần đặt
    
    private String note; // Ghi chú thêm

    private VehicleType selectedVehicleType;

    private BigDecimal confirmedPrice;
}
