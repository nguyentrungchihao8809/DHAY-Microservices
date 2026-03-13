package com.duan.hday.dto.request.driver;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TripCreateDTO {

    // --- Thông tin Vị trí ---
    @NotBlank(message = "Địa điểm đi không được để trống")
    private String startAddress;

    @NotNull(message = "Tọa độ điểm đi (Lat) là bắt buộc")
    @DecimalMin(value = "-90.0", message = "Vĩ độ không hợp lệ")
    @DecimalMax(value = "90.0", message = "Vĩ độ không hợp lệ")
    private Double startLat;

    @NotNull(message = "Tọa độ điểm đi (Lng) là bắt buộc")
    @DecimalMin(value = "-180.0", message = "Kinh độ không hợp lệ")
    @DecimalMax(value = "180.0", message = "Kinh độ không hợp lệ")
    private Double startLng;

    @NotBlank(message = "Địa điểm đến không được để trống")
    private String endAddress;

    @NotNull(message = "Tọa độ điểm đến (Lat) là bắt buộc")
    private Double endLat;

    @NotNull(message = "Tọa độ điểm đến (Lng) là bắt buộc")
    private Double endLng;

    // --- Thông tin Chuyến đi ---
    @NotNull(message = "Thời gian khởi hành không được để trống")
    @Future(message = "Thời gian khởi hành phải là một thời điểm trong tương lai")
    private LocalDateTime departureTime;
    
    @NotNull(message = "Vui lòng chọn xe")
    private Long vehicleId; 

    @NotNull(message = "Số ghế không được để trống")
    @Min(value = 1, message = "Chuyến đi phải có ít nhất 1 chỗ trống")
    @Max(value = 50, message = "Số ghế quá lớn so với quy định")
    private Integer totalSeats;

    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String note;
}