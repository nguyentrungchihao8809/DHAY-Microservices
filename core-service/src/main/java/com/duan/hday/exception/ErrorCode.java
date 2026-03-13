package com.duan.hday.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Auth & User
    USER_NOT_FOUND("USER_001", "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    ACCOUNT_LOCKED("USER_002", "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),
    
    // Driver & Vehicle
    ALREADY_DRIVER("DRV_001", "Bạn đã đăng ký làm tài xế trước đó rồi", HttpStatus.BAD_REQUEST),
    VEHICLE_PLATE_EXISTS("DRV_002", "Biển số xe này đã được đăng ký", HttpStatus.CONFLICT),
    INVALID_VEHICLE_OWNER("TRIP_002", "Xe này không thuộc hồ sơ của bạn", HttpStatus.FORBIDDEN),
    
    // Trip
    TRIP_NOT_FOUND("TRIP_003", "Không tìm thấy chuyến đi", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACTION("TRIP_004", "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    TRIP_OVERLAPPING("TRIP_001", "Lịch trình bị trùng với chuyến đi khác", HttpStatus.BAD_REQUEST),
    
    // System
    UNCATEGORIZED_EXCEPTION("SYS_999", "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("SYS_001", "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),

    ACCESS_DENIED("SYS_1003", "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}