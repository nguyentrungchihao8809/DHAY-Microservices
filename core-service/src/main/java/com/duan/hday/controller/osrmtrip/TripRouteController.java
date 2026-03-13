package com.duan.hday.controller.osrmtrip;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.duan.hday.integration.OsrmRouteDTO;
import com.duan.hday.entity.Trip;
import com.duan.hday.exception.AppException;
import com.duan.hday.exception.ErrorCode;
import com.duan.hday.repository.trip.TripRepository;
import com.duan.hday.service.OsrmService;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import com.duan.hday.dto.request.routes.TripConfirmRouteRequest;
import com.duan.hday.service.TripService;
import com.duan.hday.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.duan.hday.config.UserPrincipal;
import com.duan.hday.dto.response.RouteOptionDTO;


@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
    public class TripRouteController {

        private final OsrmService osrmService;
        private final TripRepository tripRepository;
        private final TripService tripService;

        @GetMapping("/{id}/suggested-routes")
    public ResponseEntity<?> getSuggestedRoutes(@PathVariable Long id) {
        // 1. Lấy Trip từ DB
        Trip trip = tripRepository.findTripWithLocations(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chuyến đi"));

        // 2. Gọi OSRM lấy các tuyến đường thô (Raw Routes)
        List<OsrmRouteDTO> osrmRoutes = osrmService.getAlternativeRoutes(
                trip.getStartLocation().getLat(), 
                trip.getStartLocation().getLng(), 
                trip.getEndLocation().getLat(), 
                trip.getEndLocation().getLng()
        );

        // 3. Xử lý Hotspots & Ranking tại Service
        List<RouteOptionDTO> response = tripService.handleHotspotsAndRanking(trip, osrmRoutes);

        // 4. Trả về cho FE
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirmTripRoute(
            @PathVariable Long id,
            @RequestBody TripConfirmRouteRequest dto,
            @AuthenticationPrincipal UserPrincipal principal) { // Inject UserPrincipal

        // Lấy đối tượng User đã được em đóng gói sẵn trong UserPrincipal
        User driver = principal.getUser(); 

        if (driver == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Truyền driver vào service
        tripService.confirmRoute(id, dto, driver);
        
        return ResponseEntity.ok("Xác nhận lộ trình thành công!");
    }
}