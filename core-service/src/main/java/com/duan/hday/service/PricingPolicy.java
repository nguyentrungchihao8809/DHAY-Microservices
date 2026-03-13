package com.duan.hday.service;

import com.duan.hday.entity.enums.VehicleType;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class PricingPolicy {
    // Giá mỗi KM cho từng loại xe
    private static final Map<VehicleType, BigDecimal> RATES = Map.of(
        VehicleType.MOTORBIKE, new BigDecimal("3000"),
        VehicleType.CAR_4_SEATER, new BigDecimal("8000"),
        VehicleType.CAR_7_SEATER, new BigDecimal("12000")
    );

    public BigDecimal getRate(VehicleType type) {
        return RATES.getOrDefault(type, new BigDecimal("5000"));
    }
}
