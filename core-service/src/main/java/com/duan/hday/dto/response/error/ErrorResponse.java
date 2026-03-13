package com.duan.hday.dto.response.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Builder
public class ErrorResponse {
    private int status;      // Mã lỗi HTTP (400, 401, 500,...)
    private String error;    // Loại lỗi (Bad Request, Not Found,...)
    private String message;  // Thông báo cụ thể cho User
    private String details;
    private LocalDateTime timestamp;
}