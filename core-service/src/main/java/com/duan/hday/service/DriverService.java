package com.duan.hday.service;

import com.duan.hday.entity.DriverProfile;
import com.duan.hday.entity.User;
import com.duan.hday.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.duan.hday.repository.driver.DriverProfileRepository;
import com.duan.hday.repository.driver.VehicleRepository;
import com.duan.hday.repository.auth.UserRepository;
import com.duan.hday.dto.request.driver.DriverRegistrationRequest;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;

    public boolean isDriver(Long userId) {
        return driverProfileRepository.existsById(userId);
    }

    @Transactional
    public Long registerAsDriver(Long userId, DriverRegistrationRequest request) {
        // 1. Kiểm tra User tồn tại (Fail-fast)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // 2. Kiểm tra tính hợp lệ của tài khoản
        if (!user.getIsActive() || user.getIsDeleted()) {
            throw new RuntimeException("Tài khoản người dùng đã bị khóa hoặc bị xóa");
        }

        // 3. Kiểm tra xem đã đăng ký chưa
        if (driverProfileRepository.existsById(userId)) {
            throw new RuntimeException("Bạn đã đăng ký làm tài xế trước đó rồi");
        }

        // 4. Tạo Driver Profile
        DriverProfile profile = DriverProfile.builder()
                .userId(userId) // Đảm bảo MapsId đồng bộ đúng PK/FK
                .user(user)
                .licenseNumber(request.getLicenseNumber())
                .isActive(false) // Senior Tip: Mặc định là false để Admin duyệt
                .ratingAvg(5.0)
                .totalTrips(0)
                .build();
        driverProfileRepository.save(profile);

        // 5. Kiểm tra biển số xe trùng lặp
        if (vehicleRepository.existsByVehiclePlate(request.getVehiclePlate())) {
            throw new RuntimeException("Biển số xe này đã được đăng ký trên hệ thống");
        }

        // 6. Tạo Vehicle
        Vehicle vehicle = Vehicle.builder()
                .driver(user)
                .vehiclePlate(request.getVehiclePlate())
                .vehicleBrand(request.getVehicleBrand())
                .vehicleModel(request.getVehicleModel())
                .vehicleType(request.getVehicleType())
                .capacity(request.getCapacity())
                .isVerified(false) // Chờ duyệt
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        
        // TRẢ VỀ ID: Thay đổi từ void sang Long ở đây
        return savedVehicle.getId(); 
    }
}