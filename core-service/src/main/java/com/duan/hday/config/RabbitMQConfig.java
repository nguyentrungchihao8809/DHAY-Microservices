//Cấu hình các hàng đợi (Queue) để nhận thông báo thanh toán thành công từ Payment-service.
package com.duan.hday.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.payment-success-queue}")
    private String queueName;

    @Value("${app.rabbitmq.payment-exchange}")
    private String exchange;

    @Bean
    public Queue paymentQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding binding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with("payment.completed");
    }
}
