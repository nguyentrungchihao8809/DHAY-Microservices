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
        // 1. Tìm user (User này hiện đang ở trạng thái Managed)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // 2. Validate (Giữ nguyên logic của bạn)
        if (!user.getIsActive() || user.getIsDeleted()) {
            throw new RuntimeException("Tài khoản không hợp lệ");
        }
        if (driverProfileRepository.existsById(userId)) {
            throw new RuntimeException("Bạn đã đăng ký rồi");
        }

        // 3. Tạo Driver Profile và gán vào User (Không gọi repository.save ở đây)
        DriverProfile profile = DriverProfile.builder()
                .user(user) // @MapsId sẽ tự lấy ID từ user
                .licenseNumber(request.getLicenseNumber())
                .isActive(false)
                .build();
        
        // Gán ngược lại cho user để đảm bảo tính nhất quán của object graph
        user.setDriverProfile(profile);

        // 4. Kiểm tra biển số
        if (vehicleRepository.existsByVehiclePlate(request.getVehiclePlate())) {
            throw new RuntimeException("Biển số xe đã tồn tại");
        }

        // 5. Tạo Vehicle và gán vào list của User
        Vehicle vehicle = Vehicle.builder()
                .driver(user)
                .vehiclePlate(request.getVehiclePlate())
                .vehicleBrand(request.getVehicleBrand())
                .vehicleModel(request.getVehicleModel())
                .vehicleType(request.getVehicleType())
                .capacity(request.getCapacity())
                .isVerified(false)
                .build();

        // Thay vì save lẻ tẻ, hãy để CascadeType.ALL ở User thực hiện khi kết thúc Transaction
        // Hoặc nếu muốn an toàn, chỉ save thằng Vehicle vì nó là entity độc lập hơn
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        
        return savedVehicle.getId();
    }
}