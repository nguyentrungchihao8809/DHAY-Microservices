package com.duan.hday.dto.request.driver;

import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Data
@Builder
public class PotentialPassengerResponse {
    private Long requestId;
    private Long passengerId;
    private String passengerName;
    
    // Thông tin điểm đón
    private String startAddress;
    private Double startLat;
    private Double startLng;
    
    // Thông tin điểm trả
    private String endAddress;
    private Double endLat;
    private Double endLng;
    
    private Integer seatsRequested;
    private String desiredTime;

    private double matchScore;

    @JsonIgnore
    private Point startGeom;
    
    @JsonIgnore
    private Point endGeom;   // Cần cái này để tìm vị trí trả trên route
    private boolean isSystemSuggested;
}
