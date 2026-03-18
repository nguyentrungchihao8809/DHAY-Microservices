package com.duan.payment.service;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.payment-exchange}")
    private String exchange;

    public void sendPaymentSuccess(Long bookingId) {
        // Gửi ID của booking để Core-service cập nhật CONFIRMED
        rabbitTemplate.convertAndSend(exchange, "payment.completed", bookingId);
    }
}