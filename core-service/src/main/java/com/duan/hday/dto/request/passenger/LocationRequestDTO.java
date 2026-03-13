package com.duan.hday.dto.request.passenger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequestDTO {
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
}