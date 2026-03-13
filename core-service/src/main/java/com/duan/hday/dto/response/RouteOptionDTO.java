package com.duan.hday.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteOptionDTO {
    private String polyline;
    private Double distanceKm;
    private Long durationMinutes;
    private LocalDateTime estimatedArrivalTime;
    private String routeName;
    // Thêm các trường này để mapping từ logic Ranking sang
    private Integer potentialPassengers;
    private Integer rank;
    private String description;
}