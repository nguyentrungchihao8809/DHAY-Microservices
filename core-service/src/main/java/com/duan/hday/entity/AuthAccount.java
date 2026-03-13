package com.duan.hday.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.duan.hday.entity.enums.AuthProvider;

@Entity
@Table(
    name = "auth_accounts",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "identifier"})
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthAccount implements UserDetails  { // BƯỚC 1: Thêm implements UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String identifier;

    private String passwordHash;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* ================= IMPLEMENTS USERDETAILS ================= */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Hiện tại trả về list trống (chưa phân quyền ROLE_USER, ROLE_ADMIN)
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // Spring Security sẽ dùng cái này để đối chiếu password
    }

   @Override
    public String getUsername() {
        return this.identifier;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản không bao giờ hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Tài khoản không bị khóa
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Mật khẩu không bao giờ hết hạn
    }

    @Override
    public boolean isEnabled() {
        return true; // Tài khoản luôn được kích hoạt
    }
}