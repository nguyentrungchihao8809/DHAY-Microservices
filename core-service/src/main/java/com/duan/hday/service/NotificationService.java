package com.duan.hday.service;

import com.duan.hday.entity.Booking;
import com.duan.hday.entity.Trip;
import com.duan.hday.entity.UserDevice;
import com.duan.hday.entity.enums.NotificationType;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserDeviceService userDeviceService;

    // --- 1. Thông báo cho Tài xế khi có khách đặt chỗ (BookingRequestDTO) ---
    public void sendBookingRequestToDriver(Long driverId, Booking booking) {
        String title = "\uD83D\uDE97 Yêu cầu đặt chỗ mới!";
        String body = String.format("Khách %s muốn đặt %d ghế cho chuyến đi của bạn.", 
                        booking.getPassenger().getFullName(), booking.getSeatsBooked());

        Map<String, String> payload = new HashMap<>();
        payload.put("type", NotificationType.NEW_BOOKING_REQUEST.name());
        payload.put("bookingId", booking.getId().toString());
        payload.put("tripId", booking.getTrip().getId().toString());
        payload.put("passengerName", booking.getPassenger().getFullName());
        payload.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

        sendNotification(driverId, title, body, payload);
    }

    // --- 2. Thông báo cho Khách hàng khi hệ thống tìm thấy chuyến phù hợp (Auto-matching) ---
    public void sendMatchFoundToPassenger(Long passengerId, Trip trip) {
        String title = "\uD83C\uDF89 Tìm thấy chuyến xe phù hợp!";
        String body = String.format("Tài xế %s vừa tạo chuyến đi từ %s đến %s phù hợp với yêu cầu của bạn.", 
                        trip.getDriver().getFullName(), 
                        trip.getStartLocation().getAddress(), 
                        trip.getEndLocation().getAddress());

        Map<String, String> payload = new HashMap<>();
        payload.put("type", NotificationType.MATCH_FOUND.name());
        payload.put("tripId", trip.getId().toString());
        payload.put("driverName", trip.getDriver().getFullName());
        payload.put("departureTime", trip.getDepartureTime().toString());
        
        sendNotification(passengerId, title, body, payload);
    }

    
    @Async("notificationExecutor")
    public void sendNotification(Long userId, String title, String body, Map<String, String> extraData) {
        // Lấy device thông qua Service
        List<UserDevice> devices = userDeviceService.getUserDevices(userId);
        
        if (devices.isEmpty()) {
            log.info(">>>> No registered devices for user {}", userId);
            return;
        }

        for (UserDevice device : devices) {
            try {
                Message message = buildFcmMessage(device.getFcmToken(), title, body, extraData);
                FirebaseMessaging.getInstance().send(message);
                log.info(">>>> Sent push to user {} on device {}", userId, device.getDeviceType());
            } catch (FirebaseMessagingException e) {
                handleFcmError(e, device);
            }
        }
    }

    private Message buildFcmMessage(String token, String title, String body, Map<String, String> extraData) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(notification);

        if (extraData != null) {
            builder.putAllData(extraData);
        }
        
        return builder.build();
    }

    private void handleFcmError(FirebaseMessagingException e, UserDevice device) {
        MessagingErrorCode code = e.getMessagingErrorCode();
        // Senior Tip: Chỉ xóa khi chắc chắn token không còn giá trị
        if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
            log.warn(">>>> Token expired or invalid, removing: {}", device.getFcmToken());
            userDeviceService.deleteToken(device.getFcmToken());
        } else {
            log.error(">>>> FCM Error for token {}: {}", device.getFcmToken(), e.getMessage());
        }
    }
}