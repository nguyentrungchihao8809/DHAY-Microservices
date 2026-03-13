package com.duan.hday.dto.request.auth;

import com.duan.hday.entity.enums.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 20, message = "Tên đăng nhập phải từ 4-20 ký tự")
    private String identifier;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;   // Sẽ check logic kỹ hơn trong Service cho LOCAL

    private AuthProvider provider;
}