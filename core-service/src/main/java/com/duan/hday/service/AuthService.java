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
    private final JwtService jwtService;

    /**
     * Đăng ký tài khoản LOCAL
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. Kiểm tra định danh (username/email) đã tồn tại trong hệ thống LOCAL chưa
        if (authAccountRepository.findByProviderAndIdentifier(AuthProvider.LOCAL, request.getIdentifier()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập này đã tồn tại!");
        }

        // 2. Tạo Profile User chung
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail()) // Có thể null hoặc có tùy DTO
                .isActive(true)
                .isDeleted(false)
                .build();
        user = userRepository.save(user);

        // 3. Tạo tài khoản xác thực LOCAL gắn với User
        AuthAccount authAccount = AuthAccount.builder()
                .provider(AuthProvider.LOCAL)
                .identifier(request.getIdentifier())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .user(user)
                .build();
        authAccountRepository.save(authAccount);

        return mapToResponse(user, jwtService.generateToken(authAccount));
    }

    /**
     * Đăng nhập tài khoản LOCAL
     */
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        // Sửa lại dùng hàm có JOIN FETCH để nạp User ngay lập tức
        AuthAccount authAccount = authAccountRepository
                .findByProviderAndIdentifierWithUser(AuthProvider.LOCAL, request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!"));

        if (!passwordEncoder.matches(request.getPassword(), authAccount.getPasswordHash())) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        // 3. Kiểm tra trạng thái User
        User user = authAccount.getUser();
        validateUserStatus(user);

        return mapToResponse(user, jwtService.generateToken(authAccount));
    }

    /**
     * Đăng nhập qua mạng xã hội (Google, Facebook)
     * Hỗ trợ Account Linking qua Email
     */
    @Transactional
    public UserResponse socialLogin(SocialLoginRequest request) {
        return authAccountRepository
            .findByProviderAndIdentifier(request.getProvider(), request.getIdentifier())
            .map(existingAuth -> {
                validateUserStatus(existingAuth.getUser());
                return mapToResponse(existingAuth.getUser(), jwtService.generateToken(existingAuth));
            })
            .orElseGet(() -> {
            // 1. Kiểm tra xem Email này đã có chủ chưa?
            User targetUser = null;
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                targetUser = userRepository.findByEmail(request.getEmail()).orElse(null);
            }

            // 2. Nếu chưa có User nào dùng email này -> Lúc này mới tạo User mới
            if (targetUser == null) {
                targetUser = userRepository.saveAndFlush(
                    User.createNewSocialUser(
                        request.getFullName(), 
                        request.getEmail(), 
                        request.getAvatarUrl()
                    )
                );
            }

            // 3. Tạo liên kết AuthAccount mới cho User (User cũ hoặc mới đều được)
            AuthAccount newSocialAuth = AuthAccount.builder()
                    .provider(request.getProvider())
                    .identifier(request.getIdentifier())
                    .user(targetUser) // Liên kết vào targetUser tìm được
                    .passwordHash(passwordEncoder.encode("SOCIAL_AUTH_" + java.util.UUID.randomUUID()))
                    .build();
            
            authAccountRepository.saveAndFlush(newSocialAuth);
            return mapToResponse(targetUser, jwtService.generateToken(newSocialAuth));
        });
    }

    /**
     * Kiểm tra trạng thái hoạt động của User
     */
    private void validateUserStatus(User user) {
        if (Boolean.FALSE.equals(user.getIsActive()) || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa hoặc bị xóa khỏi hệ thống!");
        }
    }

    /**
     * Chuyển đổi Entity sang DTO Response
     */
    private UserResponse mapToResponse(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(token)
                .build();
    }
}