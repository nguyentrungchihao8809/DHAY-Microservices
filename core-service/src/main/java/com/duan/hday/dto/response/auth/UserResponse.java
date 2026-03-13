package com.duan.hday.dto.response.auth;


import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String accessToken;
}
