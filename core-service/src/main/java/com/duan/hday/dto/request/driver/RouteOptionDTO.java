package com.duan.hday.dto.request.driver;

import lombok.*;

@Getter 
@Setter 
@Builder
@AllArgsConstructor 
@NoArgsConstructor
public class RouteOptionDTO {
    
    // ID định danh duy nhất cho tuyến đường này (Dùng để Client gửi lại khi Confirm)
    private String routeId;

    // Tên gợi ý cho tuyến đường (VD: "Tuyến ngắn nhất", "Tuyến nhiều khách nhất")
    private String routeName;

    // Chuỗi tọa độ mã hóa từ OSRM để Frontend vẽ lên bản đồ Mapbox
    private String polyline;

    // Tổng chiều dài quãng đường (km)
    private Double distanceKm;

    // Thời gian di chuyển dự kiến (phút)
    //Số km lệch so với tuyến ngắn nhất (Phục vụ điều kiện lọc <= 2.5km) [cite: 30]
    private Double extraDistanceKm;

    // Số lượng hành khách tiềm năng tìm thấy dọc tuyến đường qua thuật toán DBSCAN [cite: 33]
    private Integer potentialPassengers;

    // Thứ tự xếp hạng dựa trên sự tối ưu (1 là tốt nhất) 
    private Integer rank;

    // Mô tả ngắn gọn về ưu điểm của tuyến (VD: "Nhiều khách nhất nhưng đi vòng 1.5km") [cite: 24]
    private String description;
}