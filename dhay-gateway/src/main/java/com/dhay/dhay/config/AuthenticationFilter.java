package com.dhay.dhay.config;

import com.dhay.dhay.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtils jwtUtils;

    private static final String INTERNAL_CLOUD_KEY = "dhay_secret_2026";

    public AuthenticationFilter(RouteValidator validator, JwtUtils jwtUtils) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Khởi tạo các giá trị mặc định cho Header
            String userIdHeader = "GUEST";
            String identifierHeader = "ANONYMOUS";

            // 1. Nếu là Route cần bảo mật thì mới check Token
            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization Header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token Format");
                }

                String token = authHeader.substring(7);

                try {
                    // Xác thực và trích xuất thông tin từ JWT
                    jwtUtils.validateToken(token);
                    Claims claims = jwtUtils.getAllClaimsFromToken(token);
                    
                    identifierHeader = claims.getSubject();
                    Object userIdObj = claims.get("userId");
                    userIdHeader = (userIdObj != null) ? String.valueOf(userIdObj) : "UNKNOWN";

                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired or invalid");
                }
            }

            // 2. MUTATION: Tạo request mới mang đầy đủ Header bảo mật nội bộ
            // Chú ý: Tên Header phải là X-Internal-Cloud-Key để khớp với Core Service
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Internal-Key", INTERNAL_CLOUD_KEY)
                    .header("X-User-Id", userIdHeader)
                    .header("X-User-Identifier", identifierHeader)
                    .build();

            // 3. Chuyển tiếp request đã được "tiêm" Header xuống Microservices bên dưới
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        });
    }

    public static class Config {
        // Cấu hình thêm nếu cần
    }
}