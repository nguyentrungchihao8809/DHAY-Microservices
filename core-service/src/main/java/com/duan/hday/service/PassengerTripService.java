package com.duan.hday.service;

import com.duan.hday.dto.request.passenger.PassengerTripRequestDTO;
import com.duan.hday.dto.response.passenger.PriceEstimationResponse;
import com.duan.hday.entity.Location;
import com.duan.hday.entity.User;
import com.duan.hday.entity.PassengerTripRequest;
import com.duan.hday.entity.enums.NotificationType;
import com.duan.hday.entity.enums.RequestStatus;
import com.duan.hday.entity.enums.VehicleType;
import com.duan.hday.repository.auth.UserRepository;
import com.duan.hday.repository.trip.LocationRepository;
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;
import com.duan.hday.grpc.client.MatchingClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerTripService {

    private final LocationRepository locationRepository;
    private final PassengerTripRequestRepository requestRepository;
    private final UserRepository userRepository; // Dùng để lấy Proxy User
    private final OsrmService osrmService;
    private final PricingPolicy pricingPolicy;
    private final MatchingClient matchingClient;
    private final NotificationService notificationService;

    /**
     * Bước 1: Trả về các lựa chọn giá dựa trên tọa độ
     */
    public PriceEstimationResponse estimateTripPrice(Double sLat, Double sLng, Double eLat, Double eLng) {
        Double distanceKm = osrmService.getDistanceKm(sLat, sLng, eLat, eLng);
        double effectiveDistance = Math.max(distanceKm != null ? distanceKm : 0.0, 0.1);

        List<PriceEstimationResponse.VehiclePriceOption> options = Arrays.stream(VehicleType.values())
            .map(type -> {
                BigDecimal rate = pricingPolicy.getRate(type);
                BigDecimal totalPrice = rate.multiply(BigDecimal.valueOf(effectiveDistance));
                
                // Làm tròn đến hàng nghìn
                BigDecimal roundedPrice = totalPrice.divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP)
                                                .multiply(new BigDecimal("1000"));

                // Chặn giá sàn 10k
                if (roundedPrice.compareTo(new BigDecimal("10000")) < 0) {
                    roundedPrice = new BigDecimal("10000");
                }

                return new PriceEstimationResponse.VehiclePriceOption(
                    type, 
                    type.getLabel(), 
                    roundedPrice
                );
            })
            .toList();

        return PriceEstimationResponse.builder()
                .distanceKm(distanceKm)
                .options(options)
                .build();
    }

    /**
     * Bước 2: Lưu request khi khách nhấn "Đặt xe"
     */
    @Transactional
    public void processTripRequest(PassengerTripRequestDTO dto, Long passengerId) {
        // 1. CHỐNG SPAM (IDEMPOTENCY)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        
        // Chỉnh repository để nhận Long thay vì User object
        boolean hasActiveRequest = requestRepository.existsByPassengerIdAndStatusAndCreatedAtAfter(
                passengerId, RequestStatus.WAITING, fiveMinutesAgo);

        if (hasActiveRequest) {
            throw new RuntimeException("Bạn đã gửi một yêu cầu gần đây. Vui lòng đợi trong giây lát!");
        }

        // 2. TÍNH TOÁN LẠI KHOẢNG CÁCH & GIÁ (SERVER-SIDE VALIDATION)
        Double distanceKm = osrmService.getDistanceKm(dto.getStartLat(), dto.getStartLng(), dto.getEndLat(), dto.getEndLng());
        double effectiveDistance = (distanceKm == null || distanceKm <= 0) ? 0.1 : distanceKm;

        BigDecimal serverCalculatedPrice = pricingPolicy.getRate(dto.getSelectedVehicleType())
                .multiply(BigDecimal.valueOf(effectiveDistance))
                .divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("1000"));

        if (serverCalculatedPrice.compareTo(new BigDecimal("10000")) < 0) {
            serverCalculatedPrice = new BigDecimal("10000");
        }

        // 3. LẤY PROXY USER (Tiết kiệm 1 lần SELECT)
        User passengerProxy = userRepository.getReferenceById(passengerId);

        // 4. LƯU THÔNG TIN CHUYẾN ĐI
        Location startLoc = saveLocation(dto.getStartAddress(), dto.getStartLat(), dto.getStartLng());
        Location endLoc = saveLocation(dto.getEndAddress(), dto.getEndLat(), dto.getEndLng());

        PassengerTripRequest request = PassengerTripRequest.builder()
                .passenger(passengerProxy)
                .startLocation(startLoc)
                .endLocation(endLoc)
                .desiredDepartureTime(dto.getDepartureTime())
                .seatsRequested(dto.getNumberOfSeats())
                .status(RequestStatus.WAITING)
                .vehicleType(dto.getSelectedVehicleType())
                .estimatedPrice(serverCalculatedPrice) 
                .distanceKm(distanceKm)
                .build();

        PassengerTripRequest savedRequest = requestRepository.save(request);

        // 5. ĐỒNG BỘ SANG AI SERVICE (ASYNC-LIKE)
        try {
            matchingClient.syncRequestToAI(savedRequest);
            notificationService.sendTypedNotification(
                passengerId, 
                NotificationType.MATCH_FOUND, // Hoặc REQUEST_CREATED
                Map.of("requestId", savedRequest.getId().toString()),
                "Hệ thống"
            );
        } catch (Exception e) {
            log.error("Lỗi đồng bộ hoặc gửi thông báo cho Request {}: {}", savedRequest.getId(), e.getMessage());
        }
    }

    @Transactional
    public void updateTripRequest(Long requestId, PassengerTripRequestDTO dto, Long passengerId) {
        PassengerTripRequest request = findAndValidateRequest(requestId, passengerId);

        if (request.getStatus() != RequestStatus.WAITING) {
            throw new RuntimeException("Chỉ có thể chỉnh sửa yêu cầu khi đang ở trạng thái 'Đang chờ'");
        }

        updateLocation(request.getStartLocation(), dto.getStartAddress(), dto.getStartLat(), dto.getStartLng());
        updateLocation(request.getEndLocation(), dto.getEndAddress(), dto.getEndLat(), dto.getEndLng());

        request.setDesiredDepartureTime(dto.getDepartureTime());
        request.setSeatsRequested(dto.getNumberOfSeats());

        requestRepository.save(request);
    }

    @Transactional
    public void cancelTripRequest(Long requestId, Long passengerId) {
        PassengerTripRequest request = findAndValidateRequest(requestId, passengerId);

        if (request.getStatus() == RequestStatus.CANCELED) return;
        
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        
        // Thông báo cho AI để gỡ request khỏi hàng đợi matching
        // matchingClient.cancelRequestInAI(requestId); 
    }

    private PassengerTripRequest findAndValidateRequest(Long requestId, Long passengerId) {
        PassengerTripRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu ID: " + requestId));

        if (!request.getPassenger().getId().equals(passengerId)) {
            throw new RuntimeException("Bạn không có quyền thao tác trên yêu cầu này");
        }
        return request;
    }

    private Location saveLocation(String address, Double lat, Double lng) {
        Location loc = Location.builder().address(address).lat(lat).lng(lng).build();
        return locationRepository.save(loc);
    }

    private void updateLocation(Location loc, String address, Double lat, Double lng) {
        loc.setAddress(address);
        loc.setLat(lat);
        loc.setLng(lng);
        locationRepository.save(loc);
    }
}