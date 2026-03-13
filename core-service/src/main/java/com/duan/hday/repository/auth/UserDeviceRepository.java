package com.duan.hday.repository.auth;

import com.duan.hday.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.duan.hday.entity.User;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    // Thêm dòng này để tìm list device theo User ID
    List<UserDevice> findByUserId(Long userId);

    Optional<UserDevice> findByFcmToken(String token);
    Optional<UserDevice> findByUserAndFcmToken(User user, String token);
    void deleteByFcmToken(String token);
    void deleteByLastUsedAtBefore(LocalDateTime dateTime);
}