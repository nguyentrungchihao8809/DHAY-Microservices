package com.duan.hday.service;

import com.duan.hday.dto.request.driver.TripCreateDTO;
import com.duan.hday.dto.request.routes.TripConfirmRouteRequest;
import com.duan.hday.dto.response.RecentTripResponseDTO;
import com.duan.hday.dto.response.RouteOptionDTO;
import com.duan.hday.entity.Location;
import com.duan.hday.entity.Trip;
import com.duan.hday.entity.User;
import com.duan.hday.entity.Vehicle;
import com.duan.hday.entity.enums.NotificationType;
import com.duan.hday.entity.enums.TripStatus;
import com.duan.hday.exception.AppException;
import com.duan.hday.exception.ErrorCode;
import com.duan.hday.integration.OsrmRouteDTO;
import com.duan.hday.repository.auth.UserRepository;
import com.duan.hday.repository.driver.VehicleRepository;
import com.duan.hday.repository.trip.LocationRepository;
import com.duan.hday.repository.trip.TripRepository;
import com.duan.hday.grpc.client.MatchingClient;
import com.duan.hday.util.GeometryUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OsrmService osrmService;
    private final NotificationService notificationService;
    private final MatchingClient matchingClient;

    @Transactional
    public Trip createTrip(TripCreateDTO dto, Long driverId) {
        // 1. Tính toán thời gian dự kiến
        LocalDateTime startTime = dto.getDepartureTime();
        LocalDateTime estimatedEndTime = startTime.plusHours(1); 

        // 2. Kiểm tra trùng lịch (Overlap) với Lock để tránh Race Condition
        boolean isOverlapping = tripRepository.existsOverlappingTripWithLock(
                driverId, 0L, startTime, estimatedEndTime
        );

        if (isOverlapping) {
            throw new AppException(ErrorCode.TRIP_OVERLAPPING);
        }

        // 3. Kiểm tra xe
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe này"));

        if (!vehicle.getDriver().getId().equals(driverId)) {
            throw new RuntimeException("Xe này không thuộc hồ sơ của bạn!");
        }

        // 4. Lấy Proxy của User (Không query DB, chỉ lấy ID để làm khóa ngoại)
        User driverProxy = userRepository.getReferenceById(driverId);

        // 5. Tạo Trip
        Trip trip = Trip.builder()
                .driver(driverProxy)
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

    @Transactional
    public Trip confirmRoute(Long tripId, TripConfirmRouteRequest dto, Long driverId) {
        Trip trip = tripRepository.findTripWithLocations(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        boolean isOverlapping = tripRepository.existsOverlappingTripWithLock(
                driverId, tripId, trip.getDepartureTime(), dto.getEstimatedArrivalTime()
        );

        if (isOverlapping) {
            throw new AppException(ErrorCode.TRIP_OVERLAPPING);
        }

        trip.setRoutePolyline(dto.getPolyline());
        trip.setEstimatedArrivalTime(dto.getEstimatedArrivalTime());
        trip.setDistanceKm(dto.getDistanceKm());
        trip.setDurationMinutes(dto.getDurationMinutes().intValue());
        trip.setRouteName(dto.getRouteName() != null ? dto.getRouteName() : "Lộ trình không tên");
        trip.setStatus(TripStatus.OPEN); 

        Trip savedTrip = tripRepository.save(trip);
        
        // Đồng bộ sang AI Service qua gRPC
        matchingClient.syncDriverTripToAI(savedTrip);
        
        return savedTrip;
    }

    @Transactional
    public void updateTripStatus(Long tripId, TripStatus newStatus, Long driverId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        validateStatusTransition(trip.getStatus(), newStatus);

        trip.setStatus(newStatus);
        Trip savedTrip = tripRepository.save(trip);

        // Bắn thông báo cho hành khách
        if (savedTrip.getBookings() != null) {
            savedTrip.getBookings().forEach(booking -> {
                Long passengerId = booking.getPassenger().getId();
                Map<String, String> data = Map.of(
                    "tripId", tripId.toString(),
                    "status", newStatus.name()
                );

                switch (newStatus) {
                    case STARTED -> notificationService.sendTypedNotification(
                            passengerId, NotificationType.TRIP_STARTED, data, trip.getEndLocation().getAddress());
                    case COMPLETED -> notificationService.sendTypedNotification(
                            passengerId, NotificationType.TRIP_COMPLETED, data);
                    case CANCELED -> notificationService.sendTypedNotification(
                            passengerId, NotificationType.TRIP_CANCELED, data, tripId.toString());
                    default -> log.debug("No notification for status: {}", newStatus);
                }
            });
        }
    }

    public List<RouteOptionDTO> handleHotspotsAndRanking(Trip trip, List<OsrmRouteDTO> osrmRoutes) {
        if (osrmRoutes == null || osrmRoutes.isEmpty()) return Collections.emptyList();

        LocalDateTime windowStart = trip.getDepartureTime().minusMinutes(30);
        LocalDateTime windowEnd = trip.getDepartureTime().plusMinutes(30);

        List<RouteOptionDTO> routeOptions = osrmRoutes.stream()
            .map(route -> { 
                String wkt = GeometryUtils.castPolylineToWkt(route.getGeometry());
                int passengers = locationRepository.countPotentialPassengersAlongRoute(wkt, windowStart, windowEnd);

                long durationMins = Math.round(route.getDuration() / 60.0); // OSRM trả về giây, chia 60 ra phút
                LocalDateTime eta = trip.getDepartureTime().plusMinutes(durationMins);

                return RouteOptionDTO.builder()
                        .polyline(route.getGeometry())
                        .distanceKm(Math.round((route.getDistance() / 1000.0) * 100.0) / 100.0)
                        .durationMinutes(durationMins)
                        .estimatedArrivalTime(eta)
                        .routeName("Qua " + osrmService.findMainStreetName(route))
                        .potentialPassengers(passengers)
                        .build();
            })
            .sorted(java.util.Comparator
                    .comparing(RouteOptionDTO::getPotentialPassengers).reversed()
                    .thenComparing(RouteOptionDTO::getDistanceKm))
            .collect(Collectors.toList());

        for (int i = 0; i < routeOptions.size(); i++) {
            RouteOptionDTO dto = routeOptions.get(i);
            dto.setRank(i + 1);
            dto.setDescription(dto.getPotentialPassengers() > 0 
                ? "Tối ưu thu nhập: " + dto.getPotentialPassengers() + " khách dọc đường" 
                : "Tuyến đường nhanh nhất");
        }

        return routeOptions;
    }

    public List<RecentTripResponseDTO> getRecentTrips(Long driverId) {
        List<Trip> trips = tripRepository.findTop4ByDriverIdOrderByCreatedAt(driverId, PageRequest.of(0, 4));

        return trips.stream()
                .map(trip -> RecentTripResponseDTO.builder()
                        .id(trip.getId())
                        .distanceKm(trip.getDistanceKm())
                        .durationMinutes(trip.getDurationMinutes())
                        .startAddress(trip.getStartLocation().getAddress())
                        .endAddress(trip.getEndLocation().getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    private Location buildLocation(String address, Double lat, Double lng) {
        return Location.builder().address(address).lat(lat).lng(lng).build();
    }

    private void validateStatusTransition(TripStatus current, TripStatus next) {
        if (current == TripStatus.COMPLETED || current == TripStatus.CANCELED) {
            throw new RuntimeException("Chuyến đi đã kết thúc.");
        }
        if (next == TripStatus.STARTED && (current != TripStatus.OPEN && current != TripStatus.FULL)) {
            throw new RuntimeException("Chuyến đi phải ở trạng thái OPEN hoặc FULL để bắt đầu.");
        }
        if (next == TripStatus.COMPLETED && current != TripStatus.STARTED) {
            throw new RuntimeException("Phải bắt đầu chuyến đi trước khi hoàn thành.");
        }
    }
}