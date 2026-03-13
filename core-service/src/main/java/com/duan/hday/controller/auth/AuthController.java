package com.duan.hday.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.duan.hday.dto.request.auth.LoginRequest;
import com.duan.hday.dto.request.auth.RegisterRequest;
import com.duan.hday.dto.request.auth.SocialLoginRequest;
import com.duan.hday.dto.response.auth.UserResponse;
import com.duan.hday.service.AuthService;

import jakarta.validation.Valid;

@RestController //báo cho spring biết đây là một nơi tiếp nhận yêu cầu Web, biến data --> JSON
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor //Đây là "phép thuật" của lombok, tự động tạo constructor với tất cả các field final
public class AuthController {

    private final AuthService authService; //gọi service để xử lý nghiệp vụ

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) { // @RequestBody: báo cho spring lấy dữ liệu từ body của request và map vào object, ở đây là RegisterRequest, ResposeEntity<UserResponse>: trả về dữ liệu dạng JSON
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/social-login")
    public ResponseEntity<UserResponse> socialLogin(
        @Valid @RequestBody SocialLoginRequest request
        ) {
    // Gọi sang Service xử lý logic "Nếu chưa có thì tạo, có rồi thì đăng nhập"
    UserResponse response = authService.socialLogin(request);
    
    return ResponseEntity.ok(response);
    }
}
