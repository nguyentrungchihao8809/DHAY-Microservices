package com.duan.hday.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.duan.hday.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Spring Security cung cấp

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
    
    // 1. Lấy chuỗi Authorization từ Header
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    // 2. Kiểm tra xem có Token không
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    try {
    jwt = authHeader.substring(7);
    userEmail = jwtService.extractUsername(jwt); 

    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        
        // CHỖ NÀY QUAN TRỌNG: 
        // Nhờ em đã sửa ApplicationConfig, userDetails ở đây bây giờ 
        // chính là đối tượng UserPrincipal (chứa cả User entity và userId).
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
        
        if (jwtService.isTokenValid(jwt, userDetails)) {
            // SỬA TẠI ĐÂY: 
            // Thay vì truyền userDetails chung chung, việc truyền UserPrincipal 
            // vào đây giúp các Controller sau này dùng @AuthenticationPrincipal 
            // sẽ nhận được đúng Object mình cần.
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, // Đây chính là UserPrincipal
                    null,
                    userDetails.getAuthorities()
            );
            
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Lưu vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
        request.setAttribute("jwt_error", "Token đã hết hạn sử dụng!");
    } catch (io.jsonwebtoken.security.SignatureException e) {
        request.setAttribute("jwt_error", "Chữ ký Token không hợp lệ!");
    } catch (io.jsonwebtoken.MalformedJwtException e) {
        request.setAttribute("jwt_error", "Cấu trúc Token bị sai!");
    } catch (Exception e) {
        request.setAttribute("jwt_error", "Lỗi xác thực Token: " + e.getMessage());
    }

    // 7. Cho phép request đi tiếp (Vô cùng quan trọng: Phải nằm ngoài try-catch)
    filterChain.doFilter(request, response);
}
}
