package com.duan.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
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

    // Map chính xác với cột fcm_token trong DB dùng chung
    @Column(name = "fcm_token", nullable = false, length = 500)
    private String deviceToken;

    @Column(name = "device_type")
    private String deviceType; // "android", "ios", "web"

    @Column(name = "last_used_at")
    private LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        lastActive = LocalDateTime.now();
    }
}