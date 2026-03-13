package com.duan.hday.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Quan trọng: Kích hoạt tính năng bất đồng bộ
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Số lượng thread luôn sẵn sàng
        executor.setMaxPoolSize(10); // Số lượng thread tối đa khi quá tải
        executor.setQueueCapacity(100); // Hàng đợi chờ xử lý
        executor.setThreadNamePrefix("FCM-Thread-");
        executor.initialize();
        return executor;
    }
}