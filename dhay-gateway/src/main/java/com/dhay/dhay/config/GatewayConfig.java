package com.dhay.dhay.config;

import com.dhay.dhay.util.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter authFilter; // Tiêm filter của bạn vào

    @Bean
    public RouteLocator authRouteLocator(RouteLocatorBuilder builder, JwtUtils jwtUtils) {
        return builder.routes()
            .route("dhay-core-auth", r -> r.path("/api/v1/auth/**")
                .filters(f -> f
                    // BƯỚC 1: Chạy qua AuthenticationFilter để đóng dấu X-Internal-Key
                    .filter(authFilter.apply(new AuthenticationFilter.Config()))
                    // BƯỚC 2: Đợi Core trả về rồi mới tiêm Token
                    .modifyResponseBody(UserResponse.class, UserResponse.class,
                        (exchange, response) -> {
                            if (exchange.getResponse().getStatusCode() != null 
                                && exchange.getResponse().getStatusCode().is2xxSuccessful()
                                && response != null) {
                                
                                // Tạo token từ ID của User vừa login/register thành công
                                String token = jwtUtils.generateToken(response.getId().toString());
                                response.setAccessToken(token);
                            }
                            return Mono.just(response);
                        }))
                .uri("lb://dhay-core-service"))
            .build();
    }

    // DTO để Gateway hiểu cấu trúc JSON của bạn
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private String avatarUrl;
        private String accessToken;
    }
}