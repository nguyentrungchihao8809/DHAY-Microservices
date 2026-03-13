package com.duan.hday.exception;

import com.duan.hday.dto.response.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j // Quan trọng để debug lỗi hệ thống
public class GlobalExceptionHandler {

    // 1. Xử lý Custom Exception
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        
        // Log cảnh báo nghiệp vụ (không cần in cả stack trace để tránh rác log)
        log.warn("App Business Error: {} - {}", errorCode.getCode(), ex.getMessage());

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(buildErrorResponse(
                errorCode.getHttpStatus().value(), 
                errorCode.getCode(), 
                ex.getMessage(), 
                null)
            );
    }

    // 2. Xử lý Validation (Tối ưu việc map message)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.getMessage(),
                details)
        );
    }

    // 3. Fallback Exception - BẮT BUỘC PHẢI LOG ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        // In ra StackTrace để khi app lỗi còn biết đường mà sửa
        log.error("CRITICAL SYSTEM ERROR: ", ex); 

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Hệ thống đang bận, vui lòng quay lại sau!",
                null)
            );
    }

    // Hàm dùng chung để tạo object Response, tránh lặp lại LocalDateTime.now()
    private ErrorResponse buildErrorResponse(int status, String error, String message, String details) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
}