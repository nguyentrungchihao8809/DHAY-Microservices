package com.duan.hday.dto.request.auth;
import com.duan.hday.entity.enums.AuthProvider;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter @Setter
public class LoginRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String identifier;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private AuthProvider provider = AuthProvider.LOCAL; // Mặc định là LOCAL
}
