package com.duan.hday.controller.auth;

import com.duan.hday.service.UserDeviceService;
import lombok.RequiredArgsConstructor;

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
        // Lấy type từ request, nếu không có thì mặc định là MATCH_FOUND để test
        String typeStr = request.getType() != null ? request.getType() : "MATCH_FOUND";
        
        // Chuyển string sang Enum NotificationType
        com.duan.hday.entity.enums.NotificationType typeEnum;
        try {
            typeEnum = com.duan.hday.entity.enums.NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Type không hợp lệ! Hãy dùng: NEW_BOOKING_REQUEST, MATCH_FOUND, v.v.");
        }

        // Tạo data payload kèm theo key 'type' cho Flutter
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", typeStr); // QUAN TRỌNG: Key này để Flutter bắt logic
        data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
        
        // Nếu là NEW_BOOKING_REQUEST, Hào truyền 2 tham số: Tên và Số ghế
    if (typeStr.equals("NEW_BOOKING_REQUEST")) {
        notificationService.sendTypedNotification(
            request.getTargetUserId(), 
            typeEnum, 
            data, 
            "Hào Senior", 4 // {0} là Hào Senior, {1} là số 4
        );
    } else {
        notificationService.sendTypedNotification(
            request.getTargetUserId(), 
            typeEnum, 
            data, 
            "Hào Senior"
        );
    }
        
        return ResponseEntity.ok("Push event [" + typeStr + "] published to Redis for user: " + request.getTargetUserId());
    }
}