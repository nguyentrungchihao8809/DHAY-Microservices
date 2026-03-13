package com.duan.hday.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import org.springframework.http.HttpStatus;
import com.duan.hday.config.UserPrincipal;
import com.duan.hday.entity.User;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserPrincipal principal) {
        // 1. Kiểm tra an toàn ban đầu
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Phiên đăng nhập không hợp lệ"));
        }

        // 2. Dùng HashMap (Cho phép chứa giá trị null mà không gây crash)
        Map<String, Object> response = new java.util.HashMap<>();
        
        User userEntity = principal.getUser();

        response.put("message", "Dữ liệu đã được lấy thành công");
        response.put("userId", principal.getUserId());
        response.put("identifier", principal.getUsername());
        
        // Kiểm tra null từng trường một cách an toàn
        response.put("fullName", userEntity.getFullName() != null ? userEntity.getFullName() : "Người dùng DHAY");
        response.put("email", userEntity.getEmail()); // Nếu null trong DB thì trả về null trong JSON, không crash
        response.put("avatarUrl", userEntity.getAvatarUrl());

        return ResponseEntity.ok(response);
    }
}