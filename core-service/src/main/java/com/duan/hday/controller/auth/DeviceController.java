package com.duan.hday.controller.auth;

import com.duan.hday.service.UserDeviceService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.duan.hday.config.UserPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.duan.hday.service.NotificationService;
import java.util.Map;
import com.duan.hday.dto.request.notification.NotificationTestRequest;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final UserDeviceService userDeviceService;
    private final NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@AuthenticationPrincipal UserPrincipal principal, 
                                           @RequestParam String token, 
                                           @RequestParam(defaultValue = "android") String deviceType) {
        userDeviceService.saveDeviceToken(principal.getUser(), token, deviceType);
        return ResponseEntity.ok("Registered");
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<Void> unregister(@RequestParam String token) {
        userDeviceService.deleteToken(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test-push")
    public ResponseEntity<String> testPush(@RequestBody NotificationTestRequest request) {
        // Nếu không truyền data, khởi tạo Map trống tránh lỗi Null
        Map<String, String> payload = request.getData() != null ? request.getData() : new HashMap<>();
        
        // Luôn đảm bảo có click_action để Mobile xử lý được
        payload.putIfAbsent("click_action", "FLUTTER_NOTIFICATION_CLICK");

        notificationService.sendNotification(
            request.getTargetUserId(), 
            request.getTitle(), 
            request.getBody(), 
            payload
        );
        
        return ResponseEntity.ok("Push triggered for user: " + request.getTargetUserId());
    }
}