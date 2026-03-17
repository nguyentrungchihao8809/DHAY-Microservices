package com.duan.notification_service.service;

import java.io.IOException;

import com.duan.notification_service.dto.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;



@Service
@Slf4j
@RequiredArgsConstructor
public class RedisNotificationSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final FirebaseMessagingService firebaseService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationEvent event = objectMapper.readValue(message.getBody(), NotificationEvent.class);
            firebaseService.sendNotificationToUser(event);
        } catch (IOException e) {
            log.error("Lỗi parse JSON: {}", e.getMessage());
        }
    }
}
