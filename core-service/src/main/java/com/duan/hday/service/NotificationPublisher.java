package com.duan.hday.service;

import com.duan.hday.dto.event.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Tên topic phải khớp với bên Notification Service sẽ nghe
    private static final String NOTIFICATION_TOPIC = "hday-notifications";

    public void publish(NotificationEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(NOTIFICATION_TOPIC, message);
            log.info(">>>> Đã bắn sự kiện thông báo vào Redis cho User ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error(">>>> Lỗi khi bắn tin nhắn lên Redis: {}", e.getMessage());
        }
    }
}