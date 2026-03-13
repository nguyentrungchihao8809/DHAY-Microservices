package com.duan.hday.dto.response;

import lombok.*;
import java.util.List;

import com.duan.hday.dto.request.driver.RouteOptionDTO;

@Getter 
@Setter 
@Builder
@AllArgsConstructor 
@NoArgsConstructor
public class TripRoutesResponseDTO {

    // ID của chuyến đi (Trip) vừa được lưu vào Database
    private Long tripId;

    // Danh sách 2-3 tuyến đường gợi ý tối ưu nhất sau khi lọc
    private List<RouteOptionDTO> suggestedRoutes;

    // Thông báo trạng thái hoặc hướng dẫn cho Driver
    private String message;
}