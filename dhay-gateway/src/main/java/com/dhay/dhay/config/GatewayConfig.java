package com.dhay.dhay.config;

import com.dhay.dhay.util.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter authFilter;
    private final JwtUtils jwtUtils;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            
            // --- 1. AUTH ROUTE: Xử lý Login/Register và tự động tiêm Token vào Response Body ---
            .route("dhay-core-auth", r -> r.path("/api/v1/auth/**")
                .filters(f -> f
                    // Chạy qua filter để thêm X-Internal-Key
                    .filter(authFilter.apply(new AuthenticationFilter.Config()))
                    // Chỉnh sửa Response: Lấy User ID từ Core trả về để tạo JWT ngay tại Gateway
                    .modifyResponseBody(UserResponse.class, UserResponse.class,
                        (exchange, response) -> {
                            if (exchange.getResponse().getStatusCode() != null 
                                && exchange.getResponse().getStatusCode().is2xxSuccessful()
                                && response != null && response.getId() != null) {
                                
                                // Tạo token từ ID của User
                                String token = jwtUtils.generateToken(response.getId().toString());
                                response.setAccessToken(token);
                            }
                            return Mono.just(response);
                        }))
                .uri("lb://dhay-core-service"))

            // --- 2. SWAGGER AGGREGATOR ROUTES: Điều hướng lấy tài liệu API từ các Service con ---
            // Route cho Core Service Docs
            .route("core-service-docs", r -> r.path("/dhay-core-service/v3/api-docs/**")
                .filters(f -> f.rewritePath("/dhay-core-service/(?<path>.*)", "/${path}"))
                .uri("lb://dhay-core-service"))

            // Sửa đoạn này:
            .route("payment-service-docs", r -> r.path("/payment-service/v3/api-docs/**")
                .filters(f -> f.rewritePath("/payment-service/(?<path>.*)", "/${path}")) // VIẾT LIỀN (?<path>
                .uri("lb://payment-service"))

            .route("notification-service-docs", r -> r.path("/notification-service/v3/api-docs/**")
                .filters(f -> f.rewritePath("/notification-service/(?<path>.*)", "/${path}")) // VIẾT LIỀN (?<path>
                .uri("lb://notification-service"))

            // --- 3. CÁC ROUTE NGHIỆP VỤ KHÁC (Nếu không muốn cấu hình ở file .properties) ---
            // Bạn có thể để các route còn lại ở application.properties để linh hoạt, 
            // Gateway sẽ tự động gộp cả 2 nguồn cấu hình này lại.

            .build();
    }

    /**
     * DTO đại diện cho cấu hình JSON mà Core Service trả về khi Login thành công.
     * Gateway dựa vào đây để map dữ liệu và tiêm AccessToken.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private String avatarUrl;
        private String accessToken; // Trường này sẽ được Gateway điền giá trị
    }
}