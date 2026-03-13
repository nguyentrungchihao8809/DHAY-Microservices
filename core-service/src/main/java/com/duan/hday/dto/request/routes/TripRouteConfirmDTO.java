package com.duan.hday.dto.request.routes;


import lombok.*;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TripRouteConfirmDTO {
    
    private Long tripId;      // ID chuyến đi cần xác nhận lộ trình
    
    private String routeId;   // ID của option mà tài xế đã chọn
    
    /**
     * FE gửi lại chuỗi Polyline của tuyến đã chọn. 
     * BE sẽ lưu chuỗi này vào DB để làm căn cứ tìm khách dọc đường.
     */
    private String chosenPolyline; 
}