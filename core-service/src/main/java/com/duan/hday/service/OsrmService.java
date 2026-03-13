package com.duan.hday.service;

import com.duan.hday.integration.OsrmResponseDTO;
import com.duan.hday.integration.OsrmRouteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.duan.hday.integration.LegDTO;
import com.duan.hday.integration.StepDTO;

@Service
@Slf4j
public class OsrmService {

    private final RestTemplate restTemplate;

    // URL công cộng của OSRM (Trong thực tế dự án lớn, ta nên tự dựng Server OSRM riêng)
    private final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving/";

    public OsrmService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<OsrmRouteDTO> getAlternativeRoutes(Double startLat, Double startLng, Double endLat, Double endLng) {
        try {
            // 1. Xây dựng URL với các tham số yêu cầu
            // Định dạng: lng,lat;lng,lat (Lưu ý: OSRM nhận Kinh độ trước, Vĩ độ sau)
            String coordinates = String.format("%f,%f;%f,%f", startLng, startLat, endLng, endLat);
            
            String url = UriComponentsBuilder.fromUriString(OSRM_URL + coordinates)
                    .queryParam("alternatives", 3)
                    .queryParam("overview", "full")
                    .queryParam("geometries", "polyline")
                    .queryParam("steps", true)
                    .toUriString();

            log.info("Calling OSRM API: {}", url);

            // 2. Gọi API
            OsrmResponseDTO response = restTemplate.getForObject(url, OsrmResponseDTO.class);

            // 3. Kiểm tra kết quả
            if (response != null && "Ok".equalsIgnoreCase(response.getCode())) {
                return response.getRoutes();
            } else {
                log.error("OSRM returned error code: {}", response != null ? response.getCode() : "NULL");
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("Error while calling OSRM Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


        public String findMainStreetName(OsrmRouteDTO route) {
        if (route.getLegs() == null || route.getLegs().isEmpty()) {
            return "Đường không tên";
        }

        // Gom nhóm và tính tổng distance cho từng tên đường
        Map<String, Double> streetMap = new HashMap<>();

        for (LegDTO leg : route.getLegs()) {
            for (StepDTO step : leg.getSteps()) {
                String name = (step.getName() != null) ? step.getName().trim() : "";
                // Bỏ qua các đoạn không có tên hoặc quá ngắn
                if (name == null || name.isEmpty() || name.equalsIgnoreCase("Way")) {
                    continue;
                }
                streetMap.put(name, streetMap.getOrDefault(name, 0.0) + step.getDistance());
            }
        }

        // Trả về tên đường có tổng distance lớn nhất
        return streetMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Tuyến đường chính");
    }
    public Double getDistanceKm(Double startLat, Double startLng, Double endLat, Double endLng) {
        List<OsrmRouteDTO> routes = getAlternativeRoutes(startLat, startLng, endLat, endLng);
        
        if (routes != null && !routes.isEmpty()) {
            // OSRM trả về distance là mét, lấy route đầu tiên (tối ưu nhất)
            double distanceMeters = routes.get(0).getDistance();
            return Math.round((distanceMeters / 1000.0) * 100.0) / 100.0; // Đổi ra km và làm tròn
        }
        
        log.warn("Không tìm thấy lộ trình giữa ({}, {}) và ({}, {})", startLat, startLng, endLat, endLng);
        return 0.0;
    }
}
