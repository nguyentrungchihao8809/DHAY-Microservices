package com.duan.hday.dto.request.auth;

import com.duan.hday.entity.enums.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {
    
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Tên không được để trống")
    private String fullName;

    private String avatarUrl;

    @NotBlank(message = "Identifier (ID từ Social) không được để trống")
    private String identifier;

    @NotNull(message = "Provider phải là GOOGLE hoặc FACEBOOK")
    private AuthProvider provider;
}