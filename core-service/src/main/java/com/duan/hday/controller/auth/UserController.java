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
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token khong hop le");
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("userId", principal.getUserId());
        response.put("identifier", principal.getUsername());
        
        // Kiểm tra xem object User có tồn tại trong Principal không
        if (principal.getUser() != null) {
            response.put("fullName", principal.getUser().getFullName());
        } else {
            response.put("fullName", "User entity is null");
        }

        return ResponseEntity.ok(response);
    }
}