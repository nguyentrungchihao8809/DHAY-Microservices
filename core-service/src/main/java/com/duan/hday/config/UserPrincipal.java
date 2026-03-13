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
    private final Long userId; // ID của User thực thể
    private final String identifier; // Email hoặc Username
    private final String password;
    private final User user; // Đối tượng User đầy đủ để dùng trong Controller
    

    public UserPrincipal(AuthAccount authAccount) {
        this.userId = authAccount.getUser().getId();
        this.identifier = authAccount.getIdentifier();
        this.password = authAccount.getPasswordHash();
        this.user = authAccount.getUser();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Sau này em có thể thêm logic lấy Role từ user tại đây
        return Collections.emptyList();
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return identifier; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getIsActive(); }
}