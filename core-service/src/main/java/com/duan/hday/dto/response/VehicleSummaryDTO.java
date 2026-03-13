package com.duan.hday.dto.response;


import lombok.*;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class VehicleSummaryDTO {
    private Long vehicleId;
    private String displayName; // Kết hợp Brand + Model (VD: Honda Civic)
    private String vehiclePlate; // Biển số xe
    private String vehicleType;  // Loại xe (Xe máy, Ô tô 4 chỗ)
}