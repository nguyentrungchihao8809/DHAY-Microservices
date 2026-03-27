package com.duan.hday.controller.auth;

import com.duan.hday.config.UserPrincipal;
import com.duan.hday.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không tìm thấy phiên đăng nhập");
        }

        // Bước 1: Lấy thông tin cơ bản đã có sẵn từ Gateway truyền sang (trong Principal)
        Long userId = principal.getUserId();
        String identifier = principal.getUsername(); // Đây chính là cái identifier bạn cần

       return userRepository.findById(userId)
        .map(user -> {
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("identifier", identifier);
            response.put("fullName", user.getFullName());
            // Trả về ResponseEntity nằm trong map
            return ResponseEntity.ok((Object) response); 
        })
        // Sử dụng orElse thay vì orElseGet để tránh lỗi Supplier nếu không cần logic phức tạp
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dữ liệu người dùng không tồn tại"));
    }
}