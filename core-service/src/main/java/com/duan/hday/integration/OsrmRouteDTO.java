package com.duan.hday.integration;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OsrmRouteDTO {
    private String geometry; // Đây là chuỗi Polyline
    private Double duration; // Giây
    private Double distance; // Mét
    private List<LegDTO> legs;
}
