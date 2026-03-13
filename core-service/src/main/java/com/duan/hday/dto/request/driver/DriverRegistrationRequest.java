package com.duan.hday.dto.request.driver;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.duan.hday.entity.enums.VehicleType;

@Getter @Setter
public class DriverRegistrationRequest {

    @NotBlank(message = "Số bằng lái không được để trống")
    private String licenseNumber;

    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(regexp = "^[0-9]{2}[A-Z]-[0-9]{4,5}$", message = "Biển số xe không đúng định dạng (VD: 29A-12345)")
    private String vehiclePlate;

    @NotBlank(message = "Hãng xe không được để trống")
    private String vehicleBrand;

    @Min(value = 1, message = "Sức chứa tối thiểu là 1 chỗ")
    @Max(value = 50, message = "Sức chứa không quá 50 chỗ")
    private Integer capacity;

    @NotNull(message = "Loại xe không hợp lệ")
    private VehicleType vehicleType;

    @NotBlank(message = "Dòng xe không được để trống")
    private String vehicleModel; // Thêm trường này để fix lỗi undefined
}