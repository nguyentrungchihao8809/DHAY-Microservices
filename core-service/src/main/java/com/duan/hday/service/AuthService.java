package com.duan.hday.service;

import com.duan.hday.repository.auth.UserRepository;
import com.duan.hday.dto.request.auth.LoginRequest;
import com.duan.hday.dto.request.auth.RegisterRequest;
import com.duan.hday.dto.request.auth.SocialLoginRequest;
import com.duan.hday.dto.response.auth.UserResponse;
import com.duan.hday.entity.AuthAccount;
import com.duan.hday.entity.User;
import com.duan.hday.repository.auth.AuthAccountRepository;
import com.duan.hday.entity.enums.AuthProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;
    // ĐÃ XÓA JwtService

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (authAccountRepository.findByProviderAndIdentifier(AuthProvider.LOCAL, request.getIdentifier()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập này đã tồn tại!");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .isActive(true)
                .isDeleted(false)
                .build();
        user = userRepository.save(user);

        AuthAccount authAccount = AuthAccount.builder()
                .provider(AuthProvider.LOCAL)
                .identifier(request.getIdentifier())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .user(user)
                .build();
        authAccountRepository.save(authAccount);

        // Trả về không kèm token, Gateway sẽ xử lý việc gắn token sau
        return mapToResponse(user, null); 
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        AuthAccount authAccount = authAccountRepository
                .findByProviderAndIdentifierWithUser(AuthProvider.LOCAL, request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!"));

        if (!passwordEncoder.matches(request.getPassword(), authAccount.getPasswordHash())) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        User user = authAccount.getUser();
        validateUserStatus(user);

        // Trả về User info, token = null
        return mapToResponse(user, null);
    }

    @Transactional
    public UserResponse socialLogin(SocialLoginRequest request) {
        return authAccountRepository
            .findByProviderAndIdentifier(request.getProvider(), request.getIdentifier())
            .map(existingAuth -> {
                validateUserStatus(existingAuth.getUser());
                return mapToResponse(existingAuth.getUser(), null);
            })
            .orElseGet(() -> {
                User targetUser = null;
                if (request.getEmail() != null && !request.getEmail().isBlank()) {
                    targetUser = userRepository.findByEmail(request.getEmail()).orElse(null);
                }

                if (targetUser == null) {
                    targetUser = userRepository.saveAndFlush(
                        User.createNewSocialUser(
                            request.getFullName(), 
                            request.getEmail(), 
                            request.getAvatarUrl()
                        )
                    );
                }

                AuthAccount newSocialAuth = AuthAccount.builder()
                        .provider(request.getProvider())
                        .identifier(request.getIdentifier())
                        .user(targetUser)
                        .passwordHash(passwordEncoder.encode("SOCIAL_AUTH_" + java.util.UUID.randomUUID()))
                        .build();
                
                authAccountRepository.saveAndFlush(newSocialAuth);
                return mapToResponse(targetUser, null);
            });
    }

    private void validateUserStatus(User user) {
        if (Boolean.FALSE.equals(user.getIsActive()) || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa hoặc bị xóa khỏi hệ thống!");
        }
    }

    private UserResponse mapToResponse(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(token) // Sẽ là null
                .build();
    }
}