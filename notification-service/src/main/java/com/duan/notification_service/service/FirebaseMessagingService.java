package com.duan.notification_service.service;

import com.duan.notification_service.dto.NotificationEvent;
import com.duan.notification_service.entity.UserDevice;
import com.duan.notification_service.repository.UserDeviceRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessagingService {
    private final UserDeviceRepository deviceRepository;

    public void sendNotificationToUser(NotificationEvent event) {
        // 1. Lấy tất cả các thiết bị của User này
        List<UserDevice> devices = deviceRepository.findByUserId(event.getUserId());
        
        if (devices.isEmpty()) {
            log.warn("User {} không có thiết bị nào để nhận thông báo", event.getUserId());
            return;
        }

        for (UserDevice device : devices) {
            try {
                // TẠO CẤU HÌNH RIÊNG CHO ANDROID ĐỂ ÉP HIỆN THÔNG BÁO
                com.google.firebase.messaging.AndroidConfig androidConfig = com.google.firebase.messaging.AndroidConfig.builder()
                        .setPriority(com.google.firebase.messaging.AndroidConfig.Priority.HIGH) // Ưu tiên cao nhất
                        .setNotification(com.google.firebase.messaging.AndroidNotification.builder()
                                .setSound("default") // Có tiếng chuông
                                .setDefaultSound(true)
                                .setDefaultVibrateTimings(true)
                                .build())
                        .build();

                // TẠO CẤU HÌNH RIÊNG CHO IOS (NẾU CÓ)
                com.google.firebase.messaging.ApnsConfig apnsConfig = com.google.firebase.messaging.ApnsConfig.builder()
                        .setAps(com.google.firebase.messaging.Aps.builder()
                                .setSound("default")
                                .setContentAvailable(true)
                                .build())
                        .build();

                Message message = Message.builder()
                        .setToken(device.getDeviceToken())
                        .setNotification(Notification.builder()
                                .setTitle(event.getTitle())
                                .setBody(event.getBody())
                                .build())
                        .setAndroidConfig(androidConfig) // Thêm dòng này
                        .setApnsConfig(apnsConfig)       // Thêm dòng này
                        .putAllData(event.getData() != null ? event.getData() : new java.util.HashMap<>())
                        .build();

                // LẤY RESPONSE ĐỂ LOG CHO RÕ
                String response = FirebaseMessaging.getInstance().send(message);
                log.info(">>>> [SUCCESS] Đã gửi tới {} cho User {}. ID: {}", 
                         device.getDeviceType(), event.getUserId(), response);
                
            } catch (FirebaseMessagingException e) {
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED 
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                    deviceRepository.delete(device);
                    log.error(">>>> [TOKEN EXPIRED] Đã xóa thiết bị ID: {} do Token không còn hiệu lực", device.getId());
                } else {
                    log.error(">>>> [FIREBASE ERROR] Lỗi: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.error(">>>> [UNKNOWN ERROR] Lỗi không xác định: {}", e.getMessage());
            }
        }
    }
}