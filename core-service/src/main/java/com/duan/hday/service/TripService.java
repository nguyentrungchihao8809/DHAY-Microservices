package com.duan.hday.service;

import com.duan.hday.dto.request.driver.TripCreateDTO;
import com.duan.hday.dto.request.routes.TripConfirmRouteRequest;
import com.duan.hday.entity.Location;
import com.duan.hday.entity.Trip;
import com.duan.hday.entity.User;
import com.duan.hday.entity.Vehicle;
import com.duan.hday.entity.enums.TripStatus;
import com.duan.hday.event.TripStatusEvent;
import com.duan.hday.exception.AppException;
import com.duan.hday.exception.ErrorCode;
import com.duan.hday.integration.OsrmRouteDTO;
import com.duan.hday.repository.driver.VehicleRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.duan.hday.repository.trip.TripRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

import com.duan.hday.dto.response.RecentTripResponseDTO;
import com.duan.hday.dto.response.RouteOptionDTO;



@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final com.duan.hday.repository.trip.LocationRepository locationRepository;
    private final OsrmService osrmService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.duan.hday.grpc.client.MatchingClient matchingClient;

    @Transactional
        public void updateTripStatus(Long tripId, TripStatus newStatus, User driver) {
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

            // 1. Kiểm tra quyền sở hữu
            if (!trip.getDriver().getId().equals(driver.getId())) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }

            // 2. Validate logic chuyển trạng thái (State Transition)
            validateStatusTransition(trip.getStatus(), newStatus);

            // 3. Cập nhật
            trip.setStatus(newStatus);
            Trip savedTrip = tripRepository.save(trip);

            // 4. Phát sự kiện (Notification Service sẽ bắt được cái này để gửi cho khách)
            eventPublisher.publishEvent(new TripStatusEvent(savedTrip, newStatus));
            
            log.info("Trip {} status updated from {} to {}", tripId, trip.getStatus(), newStatus);
        }

        private void validateStatusTransition(TripStatus current, TripStatus next) {
            if (current == TripStatus.COMPLETED || current == TripStatus.CANCELED) {
                throw new RuntimeException("Chuyến đi đã kết thúc, không thể thay đổi trạng thái nữa.");
            }

            if (next == TripStatus.STARTED && (current != TripStatus.OPEN && current != TripStatus.FULL)) {
                throw new RuntimeException("Chuyến đi chỉ có thể bắt đầu khi đang ở trạng thái OPEN hoặc FULL.");
            }

            if (next == TripStatus.COMPLETED && current != TripStatus.STARTED) {
                throw new RuntimeException("Bạn phải bắt đầu chuyến đi trước khi xác nhận hoàn thành.");
            }
        }
    @Transactional
    public Trip createTrip(TripCreateDTO dto, User driver) { // Nhận trực tiếp đối tượng User
        
        // 1. Tính toán thời gian (Logic Senior: Luôn có buffer hoặc duration từ Mapbox)
        LocalDateTime startTime = dto.getDepartureTime();
        // Giả sử cộng thêm 1 tiếng buffer cho an toàn, sau này dùng API Mapbox tính duration thực tế
        LocalDateTime estimatedEndTime = startTime.plusHours(1); 

        // 2. Kiểm tra trùng lịch bằng Query tối ưu
        boolean isOverlapping = tripRepository.existsOverlappingTripWithLock(
            driver.getId(), 
            0L, // ID giả vì trip chưa lưu
            startTime, 
            estimatedEndTime
        );

        if (isOverlapping) {
            throw new AppException(ErrorCode.TRIP_OVERLAPPING);
        }

        // 3. Kiểm tra xe và quyền sở hữu
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe này"));

        if (!vehicle.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Xe này không thuộc hồ sơ của bạn!");
        }

        // 4. Tạo Trip bằng Builder
        Trip trip = Trip.builder()
            .driver(driver)
            .vehicle(vehicle)
            .startLocation(buildLocation(dto.getStartAddress(), dto.getStartLat(), dto.getStartLng()))
            .endLocation(buildLocation(dto.getEndAddress(), dto.getEndLat(), dto.getEndLng()))
            .departureTime(startTime)
            .estimatedArrivalTime(estimatedEndTime)
            .totalSeats(dto.getTotalSeats())
            .availableSeats(dto.getTotalSeats())
            .status(TripStatus.OPEN)
            .note(dto.getNote())
            .build();

        return tripRepository.save(trip);
    }

    private Location buildLocation(String address, Double lat, Double lng) {
        return Location.builder().address(address).lat(lat).lng(lng).build();
    }

    @Transactional // Bắt buộc phải có để Lock có tác dụng
    public Trip confirmRoute(Long tripId, TripConfirmRouteRequest dto, User driver) {
        
        // 1. Tìm Trip (Chưa cần khóa vội)
        Trip trip = tripRepository.findTripWithLocations(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        // 2. Kiểm tra Ownership (Phải là chủ mới được đi tiếp)
        if (!trip.getDriver().getId().equals(driver.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // 3. CHECK OVERLAP VỚI LOCK
        // Lúc này, nếu có request khác đang check cùng driverId, nó sẽ phải xếp hàng đợi ở đây.
        boolean isOverlapping = tripRepository.existsOverlappingTripWithLock(
                driver.getId(),
                tripId,
                trip.getDepartureTime(),
                dto.getEstimatedArrivalTime()
        );

        if (isOverlapping) {
            log.error("Race Condition Detect: Tài xế {} cố tình đặt lịch trùng!", driver.getId());
            throw new AppException(ErrorCode.TRIP_OVERLAPPING);
        }

        // 4. Cập nhật dữ liệu
        trip.setRoutePolyline(dto.getPolyline());
        trip.setEstimatedArrivalTime(dto.getEstimatedArrivalTime());
        trip.setDistanceKm(dto.getDistanceKm());
        trip.setDurationMinutes(dto.getDurationMinutes().intValue());
        trip.setRouteName(dto.getRouteName() != null ? dto.getRouteName() : "Lộ trình không tên");
        trip.setStatus(TripStatus.OPEN); 

        // 5. Lưu vào DB Core
        Trip savedTrip = tripRepository.save(trip);
        
        // 6. ĐỒNG BỘ SANG AI (Sửa lỗi gọi tại đây)
        // Gọi thông qua bean matchingClient đã được inject ở trên đầu class
        matchingClient.syncDriverTripToAI(savedTrip);
        
        return savedTrip;
    }

    public List<RouteOptionDTO> handleHotspotsAndRanking(Trip trip, List<OsrmRouteDTO> osrmRoutes) {
    if (osrmRoutes == null || osrmRoutes.isEmpty()) return java.util.Collections.emptyList();

    LocalDateTime windowStart = trip.getDepartureTime().minusMinutes(30);
    LocalDateTime windowEnd = trip.getDepartureTime().plusMinutes(30);

    // BƯỚC 1: Xử lý dữ liệu và gán vào biến routeOptions
    List<RouteOptionDTO> routeOptions = osrmRoutes.stream()
        .<RouteOptionDTO>map(route -> { 
            String wkt = com.duan.hday.util.GeometryUtils.castPolylineToWkt(route.getGeometry());
            int passengers = locationRepository.countPotentialPassengersAlongRoute(wkt, windowStart, windowEnd);

            long durationMins = Math.round(route.getDuration() / 40.0);
            LocalDateTime eta = trip.getDepartureTime().plusMinutes(durationMins);

            return RouteOptionDTO.builder()
                    .polyline(route.getGeometry())
                    .distanceKm(Math.round((route.getDistance() / 1000.0) * 100.0) / 100.0)
                    .durationMinutes(durationMins) // Gán giá trị này
                    .estimatedArrivalTime(eta)
                    .routeName("Qua " + osrmService.findMainStreetName(route))
                    .potentialPassengers(passengers)
                    .build();
        })
        // BƯỚC 2: Sắp xếp
        .sorted(java.util.Comparator
                .comparing(RouteOptionDTO::getPotentialPassengers).reversed()
                .thenComparing(RouteOptionDTO::getDistanceKm))
        .collect(java.util.stream.Collectors.toList());

        // BƯỚC 3: Bây giờ biến routeOptions đã tồn tại, ta có thể duyệt để gán Rank
        for (int i = 0; i < routeOptions.size(); i++) {
            RouteOptionDTO dto = routeOptions.get(i);
            dto.setRank(i + 1);
            dto.setDescription(dto.getPotentialPassengers() > 0 
                ? "Tối ưu thu nhập: " + dto.getPotentialPassengers() + " khách dọc đường" 
                : "Tuyến đường nhanh nhất");
        }

        return routeOptions;
    }
    // Trong TripService.java

        @Transactional
        public void startTrip(Long tripId, User driver) {
            // Chỉ đơn giản là gọi lại hàm update đã có sẵn để phát Event
            updateTripStatus(tripId, TripStatus.STARTED, driver);
            log.info("Driver {} started trip {}", driver.getId(), tripId);
        }

        @Transactional
        public void completeTrip(Long tripId, User driver) {
            updateTripStatus(tripId, TripStatus.COMPLETED, driver);
            log.info("Driver {} completed trip {}", driver.getId(), tripId);
        }

        @Transactional
        public Trip updateTrip(Long tripId, TripCreateDTO dto, User driver) {
            // 1. Tìm trip và kiểm tra quyền sở hữu
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

            if (!trip.getDriver().getId().equals(driver.getId())) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }

            // 2. Kiểm tra trạng thái: Chỉ cho phép sửa khi chưa bắt đầu/hủy/hoàn thành
            if (trip.getStatus() != TripStatus.OPEN && trip.getStatus() != TripStatus.FULL) {
                throw new RuntimeException("Không thể chỉnh sửa chuyến đi ở trạng thái: " + trip.getStatus().getLabel());
            }

            // 3. Kiểm tra trùng lịch (Overlap) nếu có thay đổi thời gian
            LocalDateTime newStartTime = dto.getDepartureTime();
            LocalDateTime newEndTime = newStartTime.plusHours(1); // Buffer tạm thời 1h

            boolean isOverlapping = tripRepository.existsOverlappingTripWithLock(
                    driver.getId(),
                    tripId, // Truyền ID hiện tại để loại trừ chính nó trong query check
                    newStartTime,
                    newEndTime
            );

            if (isOverlapping) {
                throw new AppException(ErrorCode.TRIP_OVERLAPPING);
            }

            // 4. Cập nhật thông tin cơ bản
            trip.setDepartureTime(newStartTime);
            trip.setNote(dto.getNote());
            
            // Nếu thay đổi số chỗ ngồi: kiểm tra xem số chỗ mới có ít hơn số khách đã đặt không
            if (dto.getTotalSeats() < (trip.getTotalSeats() - trip.getAvailableSeats())) {
                throw new RuntimeException("Số chỗ mới không thể ít hơn số hành khách đã đặt chỗ!");
            }
            
            int bookedSeats = trip.getTotalSeats() - trip.getAvailableSeats();
            trip.setTotalSeats(dto.getTotalSeats());
            trip.setAvailableSeats(dto.getTotalSeats() - bookedSeats);

            // 5. Cập nhật lại trạng thái OPEN/FULL dựa trên số chỗ mới
            if (trip.getAvailableSeats() == 0) {
                trip.setStatus(TripStatus.FULL);
            } else {
                trip.setStatus(TripStatus.OPEN);
            }

            // Lưu ý: Nếu thay đổi Start/End Lat-Lng, FE nên gọi lại quy trình gợi ý Route (Osrm)
            // Ở đây ta cập nhật Location cơ bản
            updateLocation(trip.getStartLocation(), dto.getStartAddress(), dto.getStartLat(), dto.getStartLng());
            updateLocation(trip.getEndLocation(), dto.getEndAddress(), dto.getEndLat(), dto.getEndLng());

            log.info("Driver {} updated trip {}", driver.getId(), tripId);
            return tripRepository.save(trip);
        }

        private void updateLocation(Location loc, String address, Double lat, Double lng) {
            loc.setAddress(address);
            loc.setLat(lat);
            loc.setLng(lng);
            locationRepository.save(loc);
        }


    public List<RecentTripResponseDTO> getRecentTrips(User driver) {
    // Sử dụng PageRequest để giới hạn 4 bản ghi
        var pageable = org.springframework.data.domain.PageRequest.of(0, 4);
        List<Trip> trips = tripRepository.findTop4ByDriverIdOrderByCreatedAt(driver.getId(), pageable);

        return trips.stream()
                .map(trip -> RecentTripResponseDTO.builder()
                        .id(trip.getId())
                        .distanceKm(trip.getDistanceKm())
                        .durationMinutes(trip.getDurationMinutes())
                        .startAddress(trip.getStartLocation().getAddress())
                        .endAddress(trip.getEndLocation().getAddress())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}
