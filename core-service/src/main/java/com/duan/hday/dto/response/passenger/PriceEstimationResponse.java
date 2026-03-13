package com.duan.hday.dto.response.passenger;

import com.duan.hday.entity.enums.VehicleType;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PriceEstimationResponse {
    private Double distanceKm;
    private List<VehiclePriceOption> options;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class VehiclePriceOption {
        private VehicleType type;
        private String label;
        private BigDecimal totalPrice;
    }
}