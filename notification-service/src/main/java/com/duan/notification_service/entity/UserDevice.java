package com.duan.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices") // Phải khớp tên bảng
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // QUAN TRỌNG: Core dùng fcmToken -> DB tạo cột fcm_token
    // Chúng ta dùng name = "fcm_token" để ánh xạ đúng vào cột đó
    @Column(name = "fcm_token", nullable = false, length = 500)
    private String deviceToken; 

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "last_used_at")
    private LocalDateTime lastActive;
}