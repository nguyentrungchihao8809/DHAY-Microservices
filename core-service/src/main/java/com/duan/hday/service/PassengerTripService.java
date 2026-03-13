package com.duan.hday.service;

import com.duan.hday.dto.request.passenger.PassengerTripRequestDTO;
import com.duan.hday.dto.response.passenger.PriceEstimationResponse;
import com.duan.hday.entity.Location;
import com.duan.hday.entity.User;
import com.duan.hday.entity.PassengerTripRequest;
import com.duan.hday.entity.enums.RequestStatus;
import com.duan.hday.repository.trip.LocationRepository;
import com.duan.hday.repository.passenger.PassengerTripRequestRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Arrays;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.duan.hday.entity.enums.VehicleType;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerTripService {

    private final LocationRepository locationRepository;
    private final PassengerTripRequestRepository requestRepository;
    private final OsrmService osrmService;
    private final PricingPolicy pricingPolicy;

    private Location saveLocation(String address, Double lat, Double lng) {
        Location loc = Location.builder()
                .address(address)
                .lat(lat)
                .lng(lng)
                .build();
        return locationRepository.save(loc); // @PrePersist sẽ tự tạo Point geom
    }
    @Transactional
    public void updateTripRequest(Long requestId, PassengerTripRequestDTO dto, User passenger) {
        // 1. Tìm và kiểm tra quyền sở hữu
        PassengerTripRequest request = findAndValidateRequest(requestId, passenger);

        // 2. Chặn nếu trạng thái không phải WAITING
        if (request.getStatus() != RequestStatus.WAITING) {
            throw new RuntimeException("Chỉ có thể chỉnh sửa yêu cầu khi đang ở trạng thái 'Đang chờ'");
        }

        // 3. Cập nhật tọa độ & địa chỉ
        updateLocation(request.getStartLocation(), dto.getStartAddress(), dto.getStartLat(), dto.getStartLng());
        updateLocation(request.getEndLocation(), dto.getEndAddress(), dto.getEndLat(), dto.getEndLng());

        // 4. Cập nhật thông tin chuyến đi
        request.setDesiredDepartureTime(dto.getDepartureTime());
        request.setSeatsRequested(dto.getNumberOfSeats());

        requestRepository.save(request);
    }

    @Transactional
    public void cancelTripRequest(Long requestId, User passenger) {
        // 1. Tìm và kiểm tra quyền sở hữu
        PassengerTripRequest request = findAndValidateRequest(requestId, passenger);

        // 2. Kiểm tra logic nghiệp vụ: Nếu đã hủy rồi thì không cần hủy nữa
        if (request.getStatus() == RequestStatus.CANCELED) {
            return;
        }
        
        // Nếu đã MATCHED, có thể cần thêm logic phạt hoặc thông báo tài xế ở đây
        if (request.getStatus() == RequestStatus.MATCHED) {
            // logic xử lý khi đã có tài xế nhận...
        }

        // 3. Thay đổi trạng thái thay vì xóa khỏi database
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
    }

    /**
     * Helper method để tìm request và validate chủ sở hữu
     */
    private PassengerTripRequest findAndValidateRequest(Long requestId, User passenger) {
        PassengerTripRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu chuyến đi với ID: " + requestId));

        if (!request.getPassenger().getId().equals(passenger.getId())) {
            throw new RuntimeException("Bạn không có quyền thao tác trên yêu cầu của người khác");
        }
        return request;
    }

    private void updateLocation(Location loc, String address, Double lat, Double lng) {
        loc.setAddress(address);
        loc.setLat(lat);
        loc.setLng(lng);
        locationRepository.save(loc);
    }
    /**
     * Bước 1: Trả về các lựa chọn giá dựa trên tọa độ
     */
    public PriceEstimationResponse estimateTripPrice(Double sLat, Double sLng, Double eLat, Double eLng) {
    // 1. Lấy khoảng cách từ OSRM
        Double distanceKm = osrmService.getDistanceKm(sLat, sLng, eLat, eLng);
        
        // Nếu khoảng cách quá ngắn hoặc lỗi, nên mặc định tối thiểu 0.1km để tránh giá = 0
        double effectiveDistance = Math.max(distanceKm, 0.1);

        // 2. Tính giá cho từng loại xe
        List<PriceEstimationResponse.VehiclePriceOption> options = Arrays.stream(VehicleType.values())
            .map(type -> {
                BigDecimal rate = pricingPolicy.getRate(type);
                // Tính tổng tiền: Rate * Distance
                BigDecimal totalPrice = rate.multiply(BigDecimal.valueOf(effectiveDistance));
                
                // LÀM TRÒN ĐẾN HÀNG NGHÌN ngay tại bước báo giá để user thấy số đẹp
                BigDecimal roundedPrice = totalPrice.divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP)
                                                .multiply(new BigDecimal("1000"));

                // CHẶN GIÁ SÀN (Min Price) TẠI ĐÂY
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
    public void processTripRequest(PassengerTripRequestDTO dto, User passenger) {
        // 1. CHỐNG SPAM (IDEMPOTENCY)
        // Giải thích: Tránh việc User nhấn nút đặt xe 10 lần liên tục tạo ra 10 chuyến trùng nhau.
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        boolean hasActiveRequest = requestRepository.existsByPassengerAndStatusAndCreatedAtAfter(
                passenger, RequestStatus.WAITING, fiveMinutesAgo);

        if (hasActiveRequest) {
            throw new RuntimeException("Bạn đã gửi một yêu cầu gần đây. Vui lòng đợi trong giây lát!");
        }

        // 2. TÍNH TOÁN LẠI KHOẢNG CÁCH (SERVER-SIDE VALIDATION)
        // Giải thích: Luôn dùng OSRM của Server để lấy khoảng cách thực tế, tránh Client gửi thông số ảo.
        Double distanceKm = osrmService.getDistanceKm(dto.getStartLat(), dto.getStartLng(), dto.getEndLat(), dto.getEndLng());
        double effectiveDistance = (distanceKm == null || distanceKm <= 0) ? 0.1 : distanceKm;

        // 3. XÁC THỰC GIÁ (PRICE VALIDATION)
        // Giải thích: Đây là phần quan trọng nhất để lưu đúng giá User đã thấy.
        BigDecimal serverCalculatedPrice = pricingPolicy.getRate(dto.getSelectedVehicleType())
                .multiply(BigDecimal.valueOf(effectiveDistance))
                .divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("1000"));

        // Nếu giá tính ra thấp hơn 10k, Server mặc định coi giá chuẩn là 10k.
        if (serverCalculatedPrice.compareTo(new BigDecimal("10000")) < 0) {
            serverCalculatedPrice = new BigDecimal("10000");
        }

        // So sánh với giá User gửi lên (giá họ đã thấy ở Bước 1)
        BigDecimal finalPrice;
        BigDecimal confirmedPriceFromClient = dto.getConfirmedPrice(); // Giả sử DTO có trường này

        if (confirmedPriceFromClient != null) {
            // Cho phép lệch 1 khoảng nhỏ (VD: 500đ) do làm tròn hoặc cập nhật OSRM nhẹ
            BigDecimal priceDifference = confirmedPriceFromClient.subtract(serverCalculatedPrice).abs();
            if (priceDifference.compareTo(new BigDecimal("500")) <= 0) {
                finalPrice = confirmedPriceFromClient; // Chấp nhận giá User đã thấy
            } else {
                // Nếu lệch quá nhiều (nghi vấn hack hoặc lỗi giá), dùng giá Server tính
                finalPrice = serverCalculatedPrice;
            }
        } else {
            finalPrice = serverCalculatedPrice;
        }

        // 4. LƯU THÔNG TIN CHUYẾN ĐI
        Location startLoc = saveLocation(dto.getStartAddress(), dto.getStartLat(), dto.getStartLng());
        Location endLoc = saveLocation(dto.getEndAddress(), dto.getEndLat(), dto.getEndLng());

        PassengerTripRequest request = PassengerTripRequest.builder()
                .passenger(passenger)
                .startLocation(startLoc)
                .endLocation(endLoc)
                .desiredDepartureTime(dto.getDepartureTime())
                .seatsRequested(dto.getNumberOfSeats())
                .status(RequestStatus.WAITING)
                .vehicleType(dto.getSelectedVehicleType())
                .estimatedPrice(finalPrice) // Lưu giá đã qua xác thực
                .distanceKm(distanceKm)
                .build();

        requestRepository.save(request);
        log.info("Đặt xe thành công cho User {}: {} VNĐ", passenger.getId(), finalPrice);
    }
}