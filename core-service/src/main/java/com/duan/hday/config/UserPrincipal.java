package com.duan.hday.config;

import com.duan.hday.entity.AuthAccount;
import com.duan.hday.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {
    private final Long userId;       // ID của User thực thể
    private final String identifier; // Email hoặc Username (Identifier)
    private final String password;   // Password hash (chỉ dùng khi login)
    private final User user;         // Đối tượng User đầy đủ (có thể null khi gọi qua Gateway)

    /**
     * Constructor 1: Dùng cho luồng đăng nhập truyền thống (Login/Auth Service)
     * Khi này chúng ta có đầy đủ thông tin từ Database.
     */
    public UserPrincipal(AuthAccount authAccount) {
        this.userId = authAccount.getUser().getId();
        this.identifier = authAccount.getIdentifier();
        this.password = authAccount.getPasswordHash();
        this.user = authAccount.getUser();
    }

    /**
     * Constructor 2: Dùng cho luồng Microservices qua API Gateway
     * Tạo nhanh Principal từ Header mà không cần truy vấn Database (Tối ưu hiệu suất I/O)
     */
    public UserPrincipal(Long userId, String identifier) {
        this.userId = userId;
        this.identifier = identifier;
        this.password = ""; // Không cần password ở tầng này vì Gateway đã validate xong
        this.user = null;   // Mặc định là null để tránh truy vấn DB không cần thiết
    }

    /**
     * Helper method để tạo nhanh instance từ Cloud Header
     */
    public static UserPrincipal createFromCloud(Long userId, String identifier) {
        return new UserPrincipal(userId, identifier);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Hiện tại hệ thống chưa phân quyền chi tiết, trả về danh sách trống
        // Sau này có thể truyền Role qua Header và xử lý tại đây
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return identifier;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Nếu load từ DB thì check isActive, nếu từ Gateway thì mặc định true (vì Gateway đã check)
        return user == null || user.getIsActive();
    }
}