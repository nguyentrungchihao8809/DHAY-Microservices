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

        String path = request.getRequestURI();

        // --- BƯỚC 1: LOẠI TRỪ SWAGGER ---
        // Nếu request là lấy tài liệu API, cho qua luôn không check Key
        if (path.contains("/v3/api-docs") || 
            path.contains("/swagger-ui") || 
            path.contains("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        // --- BƯỚC 2: CHECK KEY (Logic cũ của bạn) ---
        String gatewayKey = request.getHeader("X-Internal-Key");
        
        // Log để debug
        System.err.println("===> PATH: " + path);
        System.err.println("===> CORE NHẬN KEY: [" + gatewayKey + "]");

        if (gatewayKey == null || !gatewayKey.equals(internalApiKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Giao tiep noi bo bi tu choi! Path: " + path + "\"}");
            return; 
        }

        // 2. Lấy thông tin User đã được Gateway xác thực
        // 2. Lấy thông tin User đã được Gateway xác thực
        String userIdStr = request.getHeader("X-User-Id");
        String identifier = request.getHeader("X-User-Identifier");

        System.out.println(">>> Core Service nhan duoc Key: " + gatewayKey);
        System.out.println(">>> Core Service nhan duoc UserID: " + userIdStr);
        System.out.println(">>> Core Service nhan duoc Identifier: " + identifier);

        if (userIdStr != null && identifier != null) {
            Long userId;
            
            // KIỂM TRA: Nếu là khách (GUEST) thì gán ID tạm là -1, nếu không thì mới parse số
            if ("GUEST".equalsIgnoreCase(userIdStr)) {
                userId = -1L; 
            } else {
                try {
                    userId = Long.parseLong(userIdStr);
                } catch (NumberFormatException e) {
                    userId = -1L; // Đề phòng lỗi định dạng khác
                }
            }

            UserPrincipal principal = new UserPrincipal(userId, identifier);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principal, null, Collections.emptyList()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}