package com.duan.hday.controller.osrmtrip;

import com.duan.hday.dto.request.routes.TripConfirmRouteRequest;
import com.duan.hday.dto.response.RouteOptionDTO;
import com.duan.hday.entity.Trip;
import com.duan.hday.integration.OsrmRouteDTO;
import com.duan.hday.repository.trip.TripRepository;
import com.duan.hday.service.OsrmService;
import com.duan.hday.service.TripService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripRouteController {

    private final OsrmService osrmService;
    private final TripRepository tripRepository;
    private final TripService tripService;

    // Tên Header thống nhất với DriverController
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * Lấy danh sách các lộ trình gợi ý dựa trên điểm đi/đến của Trip
     */
    @GetMapping("/{id}/suggested-routes")
    public ResponseEntity<?> getSuggestedRoutes(@PathVariable Long id) {
        // 1. Lấy Trip từ DB (Cần Fetch Join locations để tránh N+1)
        Trip trip = tripRepository.findTripWithLocations(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chuyến đi với ID: " + id));

        // 2. Gọi OSRM lấy các tuyến đường thô (Raw Routes) từ Service tích hợp
        List<OsrmRouteDTO> osrmRoutes = osrmService.getAlternativeRoutes(
                trip.getStartLocation().getLat(), 
                trip.getStartLocation().getLng(), 
                trip.getEndLocation().getLat(), 
                trip.getEndLocation().getLng()
        );

        // 3. Xử lý logic nghiệp vụ: Tính toán Hotspots (điểm đông khách) và Xếp hạng
        List<RouteOptionDTO> response = tripService.handleHotspotsAndRanking(trip, osrmRoutes);

        return ResponseEntity.ok(response);
    }

    /**
     * Tài xế chọn một lộ trình cụ thể và xác nhận chuyến đi
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirmTripRoute(
            @PathVariable Long id,
            @RequestBody TripConfirmRouteRequest dto,
            @RequestHeader(USER_ID_HEADER) Long driverId
    ) {
        // Truyền driverId trực tiếp xuống service để kiểm tra quyền sở hữu trip
        tripService.confirmRoute(id, dto, driverId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Xác nhận lộ trình thành công! Chuyến đi của bạn đã sẵn sàng nhận khách."
        ));
    }
}