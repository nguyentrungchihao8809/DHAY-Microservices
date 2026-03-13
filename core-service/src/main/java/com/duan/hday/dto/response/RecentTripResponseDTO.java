package com.duan.hday.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecentTripResponseDTO {
    private Long id;
    private Double distanceKm;
    private Integer durationMinutes;
    private String startAddress;
    private String endAddress;
}