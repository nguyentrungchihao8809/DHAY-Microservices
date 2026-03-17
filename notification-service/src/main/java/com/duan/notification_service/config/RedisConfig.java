package com.duan.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.duan.notification_service.service.RedisNotificationSubscriber;

@Configuration
public class RedisConfig {

    // 1. Khai báo tên Topic (Phải khớp chính xác 100% với bên Core-Service)
    public static final String NOTIFICATION_TOPIC = "hday-notifications";

    /**
     * Cấu hình Container chứa các bộ lắng nghe tin nhắn
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Đăng ký Subscriber lắng nghe topic cụ thể
        container.addMessageListener(listenerAdapter, new ChannelTopic(NOTIFICATION_TOPIC));
        
        return container;
    }

    /**
     * Adapter này giúp kết nối class Service của bạn với hệ thống Redis Listener
     * Nó sẽ tự động gọi hàm "onMessage" trong class RedisNotificationSubscriber
     */
    @Bean
    MessageListenerAdapter listenerAdapter(RedisNotificationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }
}