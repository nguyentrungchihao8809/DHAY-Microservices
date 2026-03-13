package com.duan.hday.service;
import com.duan.hday.entity.AuthAccount;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long expirationTime;

    private Key getSigningKey() {
        // Sử dụng biến secretKey đã được inject
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(AuthAccount authAccount) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", authAccount.getUser().getId());
        if (authAccount.getUser().getEmail() != null) {
            claims.put("realEmail", authAccount.getUser().getEmail());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(authAccount.getIdentifier())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Dùng biến từ properties
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

        // 1. Hàm mà Filter đang gọi đây!
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject); 
        // Subject ở đây chính là Email mà mình đã set lúc generateToken
    }

    // 2. Hàm chung để trích xuất bất kỳ thông tin (Claim) nào
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. Hàm dùng SECRET_KEY để "mở khóa" và đọc toàn bộ nội dung Token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Dùng cái Key mà em đã sửa lỗi Base64 lúc nãy
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 4. Hàm kiểm tra Token có hợp lệ không (dùng cho bước 5 của Filter)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 5. Kiểm tra hạn sử dụng
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}