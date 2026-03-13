package com.duan.hday.dto.request.routes;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter @Setter
public class TripConfirmRouteRequest {
    private String polyline;   // Chuỗi đường đi tài xế đã chọn

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedArrivalTime; // Giờ đến thực tế từ OSRM

    private Double distanceKm;    // Thêm trường này
    private Double durationMinutes; // Thêm trường này
    private String routeName;     // Tên tuyến đường đã chọn
}