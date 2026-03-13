package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices", indexes = {
    @Index(name = "idx_user_device_user", columnList = "user_id"),
    @Index(name = "idx_user_device_token", columnList = "fcmToken")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500) // Token FCM thường khá dài
    private String fcmToken;

    private String deviceType;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
