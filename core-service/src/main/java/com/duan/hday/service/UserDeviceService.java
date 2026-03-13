package com.duan.hday.service;

import com.duan.hday.entity.User;
import com.duan.hday.entity.UserDevice;
import com.duan.hday.repository.auth.UserDeviceRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;

    public List<UserDevice> getUserDevices(Long userId) {
        return userDeviceRepository.findByUserId(userId);
    }

    @Transactional
    public void saveDeviceToken(User user, String token, String deviceType) {
        // 1. Nếu token này đã được đăng ký cho AI ĐÓ trước đây, xóa bản ghi cũ đi
        // để đảm bảo 1 token chỉ thuộc về 1 user duy nhất tại 1 thời điểm.
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User hoặc User ID không được null");
        }
        userDeviceRepository.findByFcmToken(token).ifPresent(existingDevice -> {
            if (!existingDevice.getUser().getId().equals(user.getId())) {
                userDeviceRepository.delete(existingDevice);
            }
        });

        // 2. Cập nhật hoặc tạo mới
        UserDevice device = userDeviceRepository.findByUserAndFcmToken(user, token)
                .orElse(new UserDevice());
        
        device.setUser(user);
        device.setFcmToken(token);
        device.setDeviceType(deviceType);
        device.setLastUsedAt(LocalDateTime.now());
        
        userDeviceRepository.save(device);
    }

    @Transactional
    public void deleteToken(String token) {
        userDeviceRepository.deleteByFcmToken(token);
    }

    // Thêm vào UserDeviceService
    @Scheduled(cron = "0 0 2 * * ?") // Chạy vào 2h sáng mỗi ngày
    @Transactional
    public void cleanObsoleteTokens() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(60); // 60 ngày không dùng
        userDeviceRepository.deleteByLastUsedAtBefore(threshold);
        log.info(">>>> Cleaned up old device tokens before {}", threshold);
    }
}