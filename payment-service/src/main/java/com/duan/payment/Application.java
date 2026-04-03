package com.duan.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
// CHỈ ĐỊNH RÕ PACKAGE CHỨA CLIENTS Ở ĐÂY
@EnableFeignClients(basePackages = "com.duan.payment.client") 
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}