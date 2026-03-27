package com.duan.hday.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class CloudHeaderFilter extends OncePerRequestFilter {

    @Value("${app.internal.api-key}")
    private String internalApiKey;

   @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        // THAY ĐỔI TẠI ĐÂY: Thử đọc cả 2 tên Header phổ biến
        String gatewayKey = request.getHeader("X-Internal-Key");
        if (gatewayKey == null) {
            gatewayKey = request.getHeader("X-Internal-Gateway-Key");
        }
        
        // Giữ nguyên log để kiểm tra
        System.err.println("===> CORE NHẬN KEY: [" + gatewayKey + "]");
        System.err.println("===> CONFIG KEY HIỆN TẠI: [" + internalApiKey + "]");

        if (gatewayKey == null || !gatewayKey.equals(internalApiKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Giao tiep noi bo bi tu choi!\"}");
            return; 
        }

        // 2. Lấy thông tin User đã được Gateway xác thực
        String userIdStr = request.getHeader("X-User-Id");
        String identifier = request.getHeader("X-User-Identifier");

        System.out.println(">>> Core Service nhan duoc Key: " + gatewayKey);
        System.out.println(">>> Core Service nhan duoc UserID: " + userIdStr);
        System.out.println(">>> Core Service nhan duoc Identifier: " + identifier);

        if (userIdStr != null && identifier != null) {
            UserPrincipal principal = new UserPrincipal(Long.parseLong(userIdStr), identifier);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principal, null, Collections.emptyList()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}